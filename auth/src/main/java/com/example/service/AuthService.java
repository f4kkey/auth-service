package com.example.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.HmacUtil;
import com.example.util.JwtUtil;
import com.example.util.PasswordUtil;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    private static final ExecutorService BCRYPT_EXECUTOR =
            Executors.newFixedThreadPool(
                    Math.max(2, Runtime.getRuntime().availableProcessors()),
                    Thread.ofPlatform().name("bcrypt-", 0).factory()
            );

    public static void shutdownBcryptExecutor() {
        BCRYPT_EXECUTOR.shutdown();
        try {
            if (!BCRYPT_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                BCRYPT_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            BCRYPT_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void register(String username, String password, String role) throws Exception {

        User existing = userDAO.findUserByUsername(username);
        if (existing != null) {
            throw new Exception("Username already exists");
        }

        // Offload bcrypt to the dedicated platform-thread pool.
        // .join() parks the virtual thread (releases carrier) until hashing completes.
        String hashed = CompletableFuture
                .supplyAsync(() -> PasswordUtil.hash(password), BCRYPT_EXECUTOR)
                .join();

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

        // sendAsync — non-blocking HTTP call; virtual thread parks on .join()
        // while the network I/O runs on the HttpClient's virtual-thread executor.
        HttpResponse<String> response = HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .join();

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("Failed to create bank account");
        }
    }

    public String login(String username, String password) throws Exception {

        User user = userDAO.findUserByUsername(username);

        if (user == null) {
            return null;
        }

        // Offload bcrypt verify to dedicated platform-thread pool.
        // .join() parks the virtual thread until verification completes.
        boolean ok = CompletableFuture
                .supplyAsync(() -> PasswordUtil.verify(password, user.getPassword()), BCRYPT_EXECUTOR)
                .join();

        if (!ok) {
            return null;
        }

        System.out.println("User: " + username + " ID: " + user.getId() + " logged in successfully");
        return JwtUtil.generateToken(user);
    }
}