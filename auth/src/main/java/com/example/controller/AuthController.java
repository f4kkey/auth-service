package com.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import com.example.service.AuthService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthController {

    private static final AuthService authService = new AuthService();

    public static class RegisterHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange)
                throws IOException {

            try {
                if (!exchange.getRequestMethod().equals("POST")) {

                    sendResponse(
                            exchange,
                            405,
                            jsonMessage("Method Not Allowed"));

                    return;
                }

                String body = readBody(exchange);

                JSONObject json = new JSONObject(body);

                String username = json.getString("username");

                String password = json.getString("password");

                authService.register(username, password);

                JSONObject response = new JSONObject();

                response.put("success", true);
                response.put("message", "Register success");

                sendResponse(exchange, 201, response.toString());

            } catch (Exception e) {

                e.printStackTrace();

                JSONObject error = new JSONObject();

                error.put("success", false);
                error.put("message", e.getMessage());

                sendResponse(exchange, 500, error.toString());
            }
        }
    }

    public static class LoginHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange)
                throws IOException {

            try {

                if (!exchange.getRequestMethod().equals("POST")) {

                    sendResponse(
                            exchange,
                            405,
                            jsonMessage("Method Not Allowed"));

                    return;
                }

                // Read body
                String body = readBody(exchange);

                JSONObject json = new JSONObject(body);

                String username = json.getString("username");

                String password = json.getString("password");

                String token = authService.login(username, password);
                System.out.println("Jwt token: " + token);

                if (token == null) {

                    JSONObject response = new JSONObject();

                    response.put("success", false);
                    response.put(
                            "message",
                            "Invalid username or password");

                    sendResponse(
                            exchange,
                            401,
                            response.toString());

                    return;
                }
                JSONObject response = new JSONObject();

                response.put("success", true);
                response.put("token", token);

                sendResponse(
                        exchange,
                        200,
                        response.toString());

            } catch (Exception e) {

                e.printStackTrace();

                JSONObject error = new JSONObject();

                error.put("success", false);
                error.put("message", e.getMessage());

                sendResponse(
                        exchange,
                        500,
                        error.toString());
            }
        }
    }

    private static String readBody(HttpExchange exchange)
            throws IOException {

        InputStream input = exchange.getRequestBody();

        return new String(
                input.readAllBytes(),
                StandardCharsets.UTF_8);
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response) throws IOException {

        exchange.getResponseHeaders()
                .set("Content-Type", "application/json");

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length);

        OutputStream os = exchange.getResponseBody();

        os.write(bytes);

        os.close();
    }

    private static String jsonMessage(String message) {

        JSONObject json = new JSONObject();

        json.put("message", message);

        return json.toString();
    }
}
