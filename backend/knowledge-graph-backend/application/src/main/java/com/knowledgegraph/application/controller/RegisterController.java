package com.knowledgegraph.application.controller;

import com.knowledgegraph.application.service.RegisterService;
import com.knowledgegraph.application.service.RegisterServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RegisterController {

    private static final RegisterService registerService = new RegisterServiceImpl(); // Service 层

    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/role/register", new RegisterHandler());
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            } else {
                sendResponse(exchange, 405, false, "Method Not Allowed: Only POST requests are supported.");
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            try {
                String requestBody = getRequestBody(exchange);
                JSONObject params = new JSONObject(requestBody);

                String userName = params.optString("userName", null);
                String password = params.optString("password", null);
                String role = params.optString("role", "student"); // 默认为 student

                if (userName == null || password == null) {
                    sendResponse(exchange, 400, false, "Missing required parameters: userName and password.");
                    return;
                }

                boolean isRegistered = registerService.registerUser(userName, password, role);

                if (isRegistered) {
                    sendResponse(exchange, 200, true, "User registered successfully.");
                } else {
                    sendResponse(exchange, 409, false, "User already exists.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, false, "Internal Server Error: " + e.getMessage());
            }
        }

        private String getRequestBody(HttpExchange exchange) throws IOException {
            try (InputStream inputStream = exchange.getRequestBody()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, boolean success, String message) throws IOException {
            JSONObject responseJson = new JSONObject();
            responseJson.put("code", statusCode);
            responseJson.put("success", success);
            responseJson.put("message", message);

            String response = responseJson.toString();
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
