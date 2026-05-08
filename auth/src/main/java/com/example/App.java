package com.example;

import com.sun.net.httpserver.HttpServer;
import com.example.controller.AuthController;

import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/register", new AuthController.RegisterHandler());
        server.createContext("/login", new AuthController.LoginHandler());
        // server.createContext("/verify", new AuthController.VerifyHandler());

        server.setExecutor(null);

        System.out.println("Auth Service running at 8080");

        server.start();
    }
}
