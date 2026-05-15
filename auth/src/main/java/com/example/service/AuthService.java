package com.example.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.HmacUtil;
import com.example.util.JwtUtil;
import com.example.util.PasswordUtil;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public void register(String username, String password, String role) throws Exception {

        User existing = userDAO.findUserByUsername(username);
        if (existing != null) {
            throw new Exception("Username already exists");
        }
        String hashed = PasswordUtil.hash(password);

        userDAO.createUser(username, hashed, role);

        System.out.println("User: " + username + " registered successfully with role: " + role);

        User user = userDAO.findUserByUsername(username);
        String jsonBody = "{\"id\": " + user.getId() + ", \"name\": \"" + username
                + "\", \"role\": \"USER\"}";
        String url = System.getenv("SERVER_BANK_URL") + "/user/create";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = HmacUtil.sign(timestamp, jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Accept", "application/json")
                .header("X-Internal-Signature", signature)
                .header("X-Internal-Timestamp", timestamp)
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("Failed to create bank account" + response.body());
        }
    }

    public String login(String username, String password) throws Exception {

        User user = userDAO.findUserByUsername(username);

        if (user == null) {
            return null;
        }
        boolean ok = PasswordUtil.verify(password, user.getPassword());
        if (!ok) {
            return null;
        }
        System.out.println("User: " + username + " ID: " + user.getId() + " logged in successfully");
        return JwtUtil.generateToken(user);
    }
}