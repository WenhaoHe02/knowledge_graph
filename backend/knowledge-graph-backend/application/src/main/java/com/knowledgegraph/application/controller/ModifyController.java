package com.knowledgegraph.application.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParseException;
import com.knowledgegraph.application.service.ModifyService;
import com.knowledgegraph.application.service.ModifyServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ModifyController {
    private static ModifyService modifyService = new ModifyServiceImpl();

    public static void registerEntryPoint(HttpServer server) {
        server.createContext("/api/knowledge/modify", new ModifyHandle());
    }

    static class ModifyHandle implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 支持跨域
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "PUT, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("PUT".equals(exchange.getRequestMethod())) {
                ObjectMapper objectMapper = new ObjectMapper();
                try (InputStream inputStream = exchange.getRequestBody()) {
                    String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                    String result = modifyService.modifyKnowledge(requestBody);
                    System.out.println(result);

                    // 构造成功的响应对象
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("code", 200);
                    responseMap.put("success", true);


                    String jsonResponse = objectMapper.writeValueAsString(responseMap);

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);

                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        outputStream.write(responseBytes);
                    }
                }  catch (JsonProcessingException e) {
                    String errorResponse = "{\"error\":\"Invalid JSON format\"}";
                    exchange.sendResponseHeaders(400, errorResponse.getBytes(StandardCharsets.UTF_8).length);
                    exchange.getResponseBody().write(errorResponse.getBytes(StandardCharsets.UTF_8));
                    exchange.getResponseBody().close();
                    return;
                }catch (Exception e) {
                    // 构造错误的响应对象
                    Map<String, Object> errorResponseMap = new HashMap<>();
                    errorResponseMap.put("code", 500);
                    errorResponseMap.put("success", false);
                    errorResponseMap.put("message", e.getMessage());

                    String errorResponse = new ObjectMapper().writeValueAsString(errorResponseMap);

                    // 返回错误响应
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, errorBytes.length);

                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        outputStream.write(errorBytes);
                    }
                }
            } else {
                // 处理非 PUT 请求
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}
