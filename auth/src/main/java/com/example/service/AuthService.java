package com.example.service;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.JwtUtil;
import com.example.util.PasswordUtil;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public void register(String username, String password) throws Exception {

        User existing = userDAO.findUserByUsername(username);
        if (existing != null) {
            throw new Exception("Username already exists");
        }
        String hashed = PasswordUtil.hash(password);

        userDAO.createUser(username, hashed);
    }

    public String login(String username, String password) throws Exception {

        String hash = userDAO.findPasswordByUsername(username);

        if (hash == null) {
            return null;
        }
        boolean ok = PasswordUtil.verify(password, hash);
        if (!ok) {
            return null;
        }

        return JwtUtil.generateToken(username);
    }
}