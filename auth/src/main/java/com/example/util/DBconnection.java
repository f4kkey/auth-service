package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBconnection {
    private static final String URL = System.getenv("DB_URL");

    private static final String USER = System.getenv("DB_USERNAME");

    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
