package com.dvdev.jdbc.starter.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManagerForPool {

    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    //исп. потокобезопасную коллекцию для connection pool
    private static BlockingQueue<Connection> pool; //тут лежат прокси соединения
    private static List<Connection> sourceConnections; //тут лежат исходные соединения, нужен для закрытия пула соединений в конце работы приложения

    static {
        loadDriver();
        initConnectionPool();
    }

    //метод инициализирует пул и возвращает соединение после его использования в пул
    private static void initConnectionPool() {
        var poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
        var size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);
        sourceConnections = new ArrayList<>();
        for (int i = 0; i < size; i++) {        //инициализируем connections
            var connection = open();
            var proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionManagerForPool.class.getClassLoader(), new Class[]{Connection.class},
                    (proxy, method, args) -> method.getName().equals("close")
                            ? pool.add((Connection) proxy) : method.invoke(connection, args));
            pool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }

    private ConnectionManagerForPool() {
    }

    //закрытый метод, чтобы никто не открыл соединение извне
    private static Connection open() {
        try {
            return DriverManager.getConnection(PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //метод, к-й достает соединение из пула
    public static Connection get() {
        try {
            return pool.take(); //возвращает соединение, если оно есть в пуле
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //метод, к-й закрывает пул соединений //закрывает реальные соединения
    //прокси-соединения при вызове на них метода close() не закрываются, а
    //возвращаются в пул
    public static void closePool() {
        try {
            for (Connection sourceConnection : sourceConnections) {
                sourceConnection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
