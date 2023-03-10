package com.dvdev.jdbc.starter;

import com.dvdev.jdbc.starter.util.ConnectionManager;
import com.dvdev.jdbc.starter.util.ConnectionManagerForPool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcRunner1 {

    public static void main(String[] args) throws SQLException {
        String flightId = "2";
        var result = getTicketsByFlightId(flightId);
        System.out.println(result);
    }

    //метод возвращает все билеты по flight_id
    private static List<Long> getTicketsByFlightId(String flightId) throws SQLException {
        String sql = """
                SELECT id 
                FROM ticket
                WHERE flight_id = %s
                """.formatted(flightId);
        List<Long> result = new ArrayList<>();
        try (var connection = ConnectionManagerForPool.get();
             var statement = connection.createStatement()) {
            var resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
//            result.add(resultSet.getLong("id"));
                result.add(resultSet.getObject("id", Long.class)); //NULL safe
            }
        }
        return result;
    }
}
