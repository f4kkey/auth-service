package com.example;

import com.sun.net.httpserver.HttpServer;
import com.example.controller.AuthController;
import com.example.service.AuthService;

import java.net.InetSocketAddress;

public class App {
    public static void initCoreUser() throws Exception {
        String adminUsername = System.getenv("ADMIN_USERNAME");
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        if (adminUsername == null || adminPassword == null) {
            System.out.println("Admin credentials not set, skipping admin initialization");
            return;
        }
        boolean exists = new AuthService().login(adminUsername, adminPassword) != null;
        if (exists) {
            System.out.println("Admin user already exists, skipping admin initialization");
            return;
        }

        System.out.println("Admin user does not exist, creating admin user");
        new AuthService().register(adminUsername, adminPassword, "ADMIN");

        String shopUsername = System.getenv("SHOP_USERNAME");
        String shopPassword = System.getenv("SHOP_PASSWORD");
        if (shopUsername == null || shopPassword == null) {
            System.out.println("Shop credentials not set, skipping shop initialization");
            return;
        }
        boolean shopExists = new AuthService().login(shopUsername, shopPassword) != null;
        if (shopExists) {
            System.out.println("Shop user already exists, skipping shop initialization");
            return;
        }

        System.out.println("Shop user does not exist, creating shop user");
        new AuthService().register(shopUsername, shopPassword, "USER");
    }

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/register", new AuthController.RegisterHandler());
        server.createContext("/login", new AuthController.LoginHandler());
        // server.createContext("/verify", new AuthController.VerifyHandler());

        server.setExecutor(null);

        Thread.sleep(5000);
        initCoreUser();
        System.out.println("Auth Service running at 8080");

        server.start();
    }
}
