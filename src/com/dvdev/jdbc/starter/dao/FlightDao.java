package com.dvdev.jdbc.starter.dao;

import com.dvdev.jdbc.starter.entity.Flight;
import com.dvdev.jdbc.starter.entity.Ticket;
import com.dvdev.jdbc.starter.exception.DaoException;
import com.dvdev.jdbc.starter.util.ConnectionManagerForPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FlightDao implements Dao<Long, Flight> {

    private static final FlightDao INSTANCE = new FlightDao();

    private static final String FIND_BY_ID_SQL = """
            SELECT id,
                flight_no,
                departure_date,
                departure_airport_code,
                arrival_date,
                arrival_airport_code,
                status,
                aircraft_id
            FROM flight
            WHERE id = ?
            """;

    private FlightDao() {
    }


    public static FlightDao getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public Ticket save(Flight ticket) {
        return null;
    }

    @Override
    public void update(Flight ticket) {

    }

    @Override
    public List<Flight> findAll() {
        return null;
    }

    @Override
    public Optional<Flight> findById(Long id) {
        try (var connection = ConnectionManagerForPool.get()) {
            return findById(id, connection);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public Optional<Flight> findById(Long id, Connection connection) {
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);

            var resulSet = preparedStatement.executeQuery();
            Flight flight = null;
            if (resulSet.next()) {
                flight = new Flight(
                        resulSet.getLong("id"),
                        resulSet.getString("flight_no"),
                        resulSet.getTimestamp("departure_date").toLocalDateTime(),
                        resulSet.getString("departure_airport_code"),
                        resulSet.getTimestamp("arrival_date").toLocalDateTime(),
                        resulSet.getString("arrival_airport_code"),
                        resulSet.getInt("aircraft_id"),
                        resulSet.getString("status")
                ); //билдим flight
            }
            return Optional.ofNullable(flight);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }
}
