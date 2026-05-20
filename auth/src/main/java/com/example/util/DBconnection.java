package com.example.util; // Fixed package name

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBconnection {

    private static final HikariDataSource dataSource;

    static {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "DB credentials missing. Please set MARIADB_URL, MARIADB_USER, and MARIADB_PASSWORD environment variables.");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.setConnectionTimeout(5000);

        config.setMaximumPoolSize(60);

        config.setMaxLifetime(600000);
        config.setIdleTimeout(60000);
        config.setAutoCommit(true);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Call this inside your application's shutdown hook
     * to safely close the pool and prevent memory leaks.
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}