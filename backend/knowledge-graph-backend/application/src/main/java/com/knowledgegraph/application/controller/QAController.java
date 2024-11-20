package com.knowledgegraph.application.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.knowledgegraph.application.service.QAService;
import com.knowledgegraph.application.service.QAServiceImpl;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class QAController {

    private static final QAService qaService = new QAServiceImpl();  // 调用服务层

    public static void registerEndpoints(HttpServer server) {
        // 注册问答接口
        server.createContext("/api/qa/ask", new AskHandler());
    }

    static class AskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许跨域访问
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            // 只允许 POST 请求
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    // 获取请求体内容
                    String requestBody = getRequestBody(exchange);

                    // 如果请求体为空，返回 400 错误
                    if (requestBody == null || requestBody.trim().isEmpty()) {
                        sendResponse(exchange, 400, "Bad Request: No question provided.");
                        return;
                    }

                    // 调用服务层获取答案
                    String answer = qaService.getAnswer(requestBody);

                    // 构建响应 JSON
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);
                    responseJson.put("question", requestBody);
                    responseJson.put("answer", answer);

                    // 发送响应
                    sendResponse(exchange, 200, responseJson.toString());

                } catch (Exception e) {
                    // 处理异常，返回 500 错误
                    e.printStackTrace();
                    sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
                }
            } else {
                // 如果不是 POST 请求，返回 405 错误
                sendResponse(exchange, 405, "Method Not Allowed: Only POST requests are accepted.");
            }
        }

        // 辅助方法：读取请求体
        private String getRequestBody(HttpExchange exchange) throws IOException {
            try (InputStream inputStream = exchange.getRequestBody()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        // 辅助方法：发送响应
        private void sendResponse(HttpExchange exchange, int statusCode, String responseMessage) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, responseMessage.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
