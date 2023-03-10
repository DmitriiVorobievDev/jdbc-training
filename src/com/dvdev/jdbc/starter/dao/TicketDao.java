package com.dvdev.jdbc.starter.dao;

import com.dvdev.jdbc.starter.dto.TicketFilter;
import com.dvdev.jdbc.starter.entity.Flight;
import com.dvdev.jdbc.starter.entity.Ticket;
import com.dvdev.jdbc.starter.exception.DaoException;
import com.dvdev.jdbc.starter.util.ConnectionManagerForPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class TicketDao implements Dao<Long, Ticket> {

    private static final TicketDao INSTANCE = new TicketDao();
    ///каждый DAO содержит набор строк- команд SQL
    private static final String DELETE_SQL = """
            DELETE FROM ticket
            WHERE id = ?
            """;
    private static final String SAVE_SQL = """
            INSERT INTO ticket (passenger_no, passenger_name, flight_id, seat_no, cost)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE ticket
            SET passenger_no = ?,
                passenger_name = ?,
                flight_id = ?,
                seat_no = ?,
                cost = ?
            WHERE id = ?
            """;
    private static final String FIND_BY_ID = """
            SELECT ticket.id, 
                    passenger_no,
                    passenger_name,
                    flight_id,
                    seat_no,
                    cost,
                    f.flight_no,
                    f.status,
                    f.aircraft_id,
                    f.arrival_airport_code,
                    f.arrival_date,
                    f.departure_airport_code,
                    f.departure_date                    
            FROM ticket
            JOIN flight f 
            ON ticket.flight_id = f.id
            WHERE ticket.id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT ticket.id, 
                    passenger_no,
                    passenger_name,
                    flight_id,
                    seat_no,
                    cost,
                    f.flight_no,
                    f.status,
                    f.aircraft_id,
                    f.arrival_airport_code,
                    f.arrival_date,
                    f.departure_airport_code,
                    f.departure_date                    
            FROM ticket
            JOIN flight f 
            ON ticket.flight_id = f.id
            """;

    private final FlightDao flightDao = FlightDao.getInstance();


    private TicketDao() {
    }

    //delete 1 line from table method
    public boolean delete(Long id) {
        try (var connection = ConnectionManagerForPool.get();
             var prepareStatement = connection.prepareStatement(DELETE_SQL)
        ) {
            prepareStatement.setLong(1, id);

            return prepareStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    //insert method
    public Ticket save(Ticket ticket) {
        try (var connection = ConnectionManagerForPool.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) { //теперь preparedStatement после выполнения запроса будет возвращать ключи
            preparedStatement.setString(1, ticket.getPassengerNo()); //сетим данные вместо "?"
            preparedStatement.setString(2, ticket.getPassengerName());
            preparedStatement.setLong(3, ticket.getFlight().id());
            preparedStatement.setString(4, ticket.getSeatNo());
            preparedStatement.setBigDecimal(5, ticket.getCost());

            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                ticket.setId(generatedKeys.getLong("id")); //сетим id,полученный из резалтсета
            }
            return ticket;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    //update method - аналогично методу save, обычно их объединяют в ORM
    public void update(Ticket ticket) {
        try (var connection = ConnectionManagerForPool.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, ticket.getPassengerNo());
            preparedStatement.setString(2, ticket.getPassengerName());
            preparedStatement.setLong(3, ticket.getFlight().id());
            preparedStatement.setString(4, ticket.getSeatNo());
            preparedStatement.setBigDecimal(5, ticket.getCost());
            preparedStatement.setLong(6, ticket.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    //select method
    public Optional<Ticket> findById(Long id) {
        try (var connection = ConnectionManagerForPool.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
            preparedStatement.setLong(1, id);

            var resultSet = preparedStatement.executeQuery();
            Ticket ticket = null;
            if (resultSet.next()) { //if, а не while, т.к. можно получить только 1 одну сущность или ни одной
                ticket = buildTicket(resultSet);
            }
            return Optional.ofNullable(ticket);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    //метод билдит тикет из резалтсета
    private Ticket buildTicket(ResultSet resultSet) throws SQLException {
        var flight = new Flight(
                resultSet.getLong("flight_id"),
                resultSet.getString("flight_no"),
                resultSet.getTimestamp("departure_date").toLocalDateTime(),
                resultSet.getString("departure_airport_code"),
                resultSet.getTimestamp("arrival_date").toLocalDateTime(),
                resultSet.getString("arrival_airport_code"),
                resultSet.getInt("aircraft_id"),
                resultSet.getString("status")
        );
        return new Ticket(  //создаем объект ticket из резалтсета
                resultSet.getLong("id"),
                resultSet.getString("passenger_no"),
                resultSet.getString("passenger_name"),
                flightDao.findById(resultSet.getLong("flight_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                resultSet.getString("seat_no"),
                resultSet.getBigDecimal("cost")
        );
    }

    //метод findAll с фильтрацией, когда условие поиска может меняться
    public List<Ticket> findAll(TicketFilter filter) {
        List<Object> parameters = new ArrayList<>(); //лист вопросиков, к-е будем сетить
        List<String> whereSql = new ArrayList<>(); //коллекция с фильтрациями WHERE
        if (filter.seatNo() != null) { //используем seatNo ли параметр при фильтрации
            whereSql.add("seat_no LIKE ?");
            parameters.add("%" + filter.seatNo() + "%");
        }
        if (filter.passengerName() != null) { //используем ли параметр passengerName при фильтрации
            whereSql.add("passenger_name = ?");
            parameters.add(filter.passengerName());
        }
        parameters.add(filter.limit()); //обязательный параметр фильтрации
        parameters.add(filter.offset()); ////обязательный параметр фильтрации
        var where = whereSql.stream()
                .collect(joining(" AND ", " WHERE ", " LIMIT ? OFFSET ?"));

        var sql = FIND_ALL_SQL + where;

        try (var connection = ConnectionManagerForPool.get();
             var preparedStatement = connection.prepareStatement(sql)) { //sql динамический
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i)); //сетим ? из листа параметров
            }
            System.out.println(preparedStatement);
            var resultSet = preparedStatement.executeQuery();
            List<Ticket> tickets = new ArrayList<>(); //создаем лист тикетов по нашему запросу
            while (resultSet.next()) {
                tickets.add(buildTicket(resultSet)); //создаем тикет(запись) из резалтсета
            }
            return tickets;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    //find all method
    public List<Ticket> findAll() {
        try (var connection = ConnectionManagerForPool.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<Ticket> tickets = new ArrayList<>();
            while (resultSet.next()) {
                tickets.add(buildTicket(resultSet));
            }
            return tickets;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public static TicketDao getInstance() {
        return INSTANCE;
    }
}
