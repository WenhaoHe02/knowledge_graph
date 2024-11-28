package com.knowledgegraph.application.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import com.knowledgegraph.application.service.RoleService;
import com.knowledgegraph.application.service.RoleServiceImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RoleController {

    private static final RoleService roleService = new RoleServiceImpl(); // 调用服务层

    public static void registerEndpoints(HttpServer server) {
        // 注册 Role 接口
        server.createContext("/api/role/login", new RoleHandler());
    }

    static class RoleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    if (query == null || query.trim().isEmpty()) {
                        sendResponse(exchange, 400, false);
                        return;
                    }

                    JSONObject params = parseQueryParams(query);
                    String userName = params.optString("userName", null);
                    userName = userName.substring(1, userName.length() - 1);
                    String password = params.optString("password", null);
                    password = password.substring(1, password.length() - 1);
                    String role = params.optString("role", null);
                    role = role.substring(1, role.length() - 1);
                    System.out.println("Received parameters: userName=" + userName + ", password=" + password + ", role=" + role);


                    if (userName == null || password == null || role == null) {
                        sendResponse(exchange, 400, false);
                        return;
                    }

                    boolean isValid = roleService.validateRole(userName, password, role);
                    sendResponse(exchange, isValid ? 200 : 403, isValid);

                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, false);
                }
            } else {
                sendResponse(exchange, 405, false);
            }
        }

        private JSONObject parseQueryParams(String query) {
            JSONObject params = new JSONObject();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
            return params;
        }

        private void sendResponse(HttpExchange exchange, int statusCode, boolean success) throws IOException {
            JSONObject responseJson = new JSONObject();
            responseJson.put("code", statusCode);
            responseJson.put("success", success);

            String response = responseJson.toString();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
