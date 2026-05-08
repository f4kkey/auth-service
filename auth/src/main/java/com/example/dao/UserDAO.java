package com.example.dao;

import com.example.util.DBconnection;
import com.example.model.User;

import java.sql.*;

public class UserDAO {

    public void createUser(
            String username,
            String password) throws Exception {

        Connection conn = DBconnection.getConnection();

        String sql = "INSERT INTO users(username,password) VALUES (?,?)";

        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, username);
        stmt.setString(2, password);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    public User findUserByUsername(String username) throws Exception {

        Connection conn = DBconnection.getConnection();

        String sql = "SELECT * FROM users WHERE username=?";

        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();

        User user = null;

        if (rs.next()) {
            user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
        }

        rs.close();
        stmt.close();
        conn.close();

        return user;
    }

    public String findPasswordByUsername(
            String username) throws Exception {

        Connection conn = DBconnection.getConnection();

        String sql = "SELECT password FROM users WHERE username=?";

        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();

        String password = null;

        if (rs.next()) {
            password = rs.getString("password");
        }

        rs.close();
        stmt.close();
        conn.close();

        return password;
    }
}