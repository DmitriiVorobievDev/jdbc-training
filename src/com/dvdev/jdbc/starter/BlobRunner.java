package com.dvdev.jdbc.starter;

import com.dvdev.jdbc.starter.util.ConnectionManagerForPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;

public class BlobRunner {

    public static void main(String[] args) throws SQLException, IOException {
//        saveImage();
        getImage();
    }

    //в Postgres нет поддержки блоба, но код выглядит так, например, для Oracle
//    private static void saveImage() throws SQLException, IOException {
//        var sql = """
//                UPDATE aircraft
//                SET image = ?
//                WHERE id = 1;
//                """;
//        try(Connection connection = ConnectionManager.open();
//            var preparedStatement = connection.prepareStatement(sql)) {
//            connection.setAutoCommit(false);
//            var blob = connection.createBlob();
//            blob.setBytes(1, Files.readAllBytes(Path.of("resources", "boeing_777.png")));
//
//            preparedStatement.setBlob(1, blob);
//            preparedStatement.executeUpdate();
//            connection.commit();
//        }
//    }

    //Метод сохраняет картинку в БД как bytea
    //код для Postgres, создаем аналог блоба - bytea
    //при работе с bytea нужно вручную сделать транзакцию
    private static void saveImage() throws SQLException, IOException {
        var sql = """
                UPDATE aircraft
                SET image = ?
                WHERE id = 1;
                """;
        try (Connection connection = ConnectionManagerForPool.get();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBytes(1, Files.readAllBytes(Path.of("resources", "boeing_777.png")));
            //с клобом(TEXT для Postgres) работам как со строкой preparedStatement.setString(....)

            preparedStatement.executeUpdate();
        }
    }

    //метод - получить картинку из БД
    private static void getImage() throws SQLException, IOException {
        var sql = """
                SELECT image
                FROM aircraft
                WHERE id = ?
                """;
        try (var connection = ConnectionManagerForPool.get();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, 1);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var image = resultSet.getBytes("image");
                Files.write(Path.of("resources", "boeing777_new.png"), image, StandardOpenOption.CREATE);
            }
        }
    }
}
