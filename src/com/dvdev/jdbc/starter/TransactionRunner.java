package com.dvdev.jdbc.starter;

import com.dvdev.jdbc.starter.util.ConnectionManager;
import com.dvdev.jdbc.starter.util.ConnectionManagerForPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionRunner {

    public static void main(String[] args) throws SQLException {
        long flightId = 8;
        var deleteFlightSql = "DELETE FROM flight WHERE id = ?";
        var deleteTicketsSql = "DELETE FROM ticket WHERE flight_id = ?";
        Connection connection = null;
        PreparedStatement deleteFlightStatement = null;
        PreparedStatement deleteTicketsStatement = null;
        try {
            connection = ConnectionManagerForPool.get();
            deleteFlightStatement = connection.prepareStatement(deleteFlightSql);
            deleteTicketsStatement = connection.prepareStatement(deleteTicketsSql);

            connection.setAutoCommit(false); //auto commit turned off

            deleteFlightStatement.setLong(1, flightId);
            deleteTicketsStatement.setLong(1, flightId);

            deleteTicketsStatement.executeUpdate();
            if (true) {
                throw new RuntimeException("Oooops!");
            }
            deleteFlightStatement.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            if(connection != null) {
               connection.rollback();
            }
            throw e;
        } finally {
            if(connection != null) {
                connection.close();
            }
            if(deleteFlightStatement != null) {
                deleteFlightStatement.close();
            }
            if(deleteTicketsStatement != null) {
                deleteTicketsStatement.close();
            }
        }
    }
}

