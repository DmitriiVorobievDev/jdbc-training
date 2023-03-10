package com.dvdev.jdbc.starter;

import com.dvdev.jdbc.starter.util.ConnectionManager;
import com.dvdev.jdbc.starter.util.ConnectionManagerForPool;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PreparedStatementRunner {

    public static void main(String[] args) throws SQLException {
//        Long flightId = 2L;
//        var result = getTicketsByFlightId(flightId);
//        System.out.println(result);
//        var result = getFlightsBetween(LocalDate.of(2020, 1, 1).atStartOfDay(),
//                LocalDateTime.now());
//        System.out.println(result);
        try {
            checkMetaData();
        } finally {
            ConnectionManagerForPool.closePool();
        }

    }

    private static List<Long> getFlightsBetween(LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = """
                SELECT id
                FROM flight
                WHERE departure_date BETWEEN ? AND ?
                """;
        List<Long> result = new ArrayList<>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {

            System.out.println(preparedStatement);

            preparedStatement.setTimestamp(1, Timestamp.valueOf(start)); //первый ?
            System.out.println(preparedStatement);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(end)); //второй ?
            System.out.println(preparedStatement);

            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(resultSet.getLong("id"));
            }
        }
        return result;
    }

    private static void checkMetaData() throws SQLException {
        try (var connection = ConnectionManagerForPool.get()) {
            var metaData = connection.getMetaData();
            var catalogs = metaData.getCatalogs(); //возвращает ResultSet
            while (catalogs.next()) {
                var catalog = catalogs.getString(1); //получаем строку из 1й колонки результата
//                System.out.println(catalog);

                var schemas = metaData.getSchemas();
                while (schemas.next()) {
                    var schema = schemas.getString("TABLE_SCHEM");
//                    System.out.println(schema);

                    var tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"}); //%-все таблицы
                    if (schema.equals("public")) {
                        while (tables.next()) {
                            System.out.println(tables.getString("TABLE_NAME"));
                        }
                    }
                }
            }
        }
    }

    //метод возвращает все билеты по flight_id
    private static List<Long> getTicketsByFlightId(Long flightId) throws SQLException {
        String sql = """
                SELECT id 
                FROM ticket
                WHERE flight_id = ?
                """;
        List<Long> result = new ArrayList<>();
        try (var connection = ConnectionManagerForPool.get();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setFetchSize(50);
            preparedStatement.setQueryTimeout(10);
            preparedStatement.setMaxRows(100);

            preparedStatement.setLong(1, flightId); //вместо ?

            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
//            result.add(resultSet.getLong("id"));
                result.add(resultSet.getObject("id", Long.class)); //NULL safe
            }
        }
        return result;
    }
}
