package com.dvdev.jdbc.starter.util;

import java.io.IOException;
import java.util.Properties;

public final class PropertiesUtil {

    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    //загружаем файл application.properties
    private static void loadProperties() {
        try(var inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e); //чтобы приложение упало, если не прочитало properties-файл
        }
    }

    //метод, возвращающий property по ключу
    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private PropertiesUtil() {
    }

}
