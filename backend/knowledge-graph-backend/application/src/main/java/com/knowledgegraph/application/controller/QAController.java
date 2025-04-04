package com.knowledgegraph.application.controller;

import com.knowledgegraph.application.repository.ConversationRepository;
import com.knowledgegraph.application.service.QAServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.knowledgegraph.application.service.QAService;
import org.json.JSONObject;

import java.io.IOException;
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

            // 只允许 GET 请求
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    // 获取请求参数
                    String queryParams = exchange.getRequestURI().getQuery();
                    if (queryParams == null || queryParams.trim().isEmpty()) {
                        // 如果请求参数为空，返回 400 错误
                        sendResponse(exchange, 400, "Bad Request: No parameters provided.");
                        return;
                    }

                    String question = null;
                    String username = null;
                    for (String param : queryParams.split("&")) {
                        String[] pair = param.split("=");
                        if (pair.length == 2) {
                            if ("question".equalsIgnoreCase(pair[0])) {
                                question = pair[1];
                            } else if ("username".equalsIgnoreCase(pair[0])) {
                                username = pair[1];
                            }
                        }
                    }

                    // 校验参数
                    if (question == null || question.trim().isEmpty()) {
                        sendResponse(exchange, 400, "Bad Request: No question provided.");
                        return;
                    }

                    if (username == null || username.trim().isEmpty()) {
                        sendResponse(exchange, 400, "Bad Request: No username provided.");
                        return;
                    }

                    // 调用服务层获取答案
                    String answer = qaService.getAnswer(question);

                    // 创建会话
                    ConversationRepository conversationRepository = new ConversationRepository();
                    int conversationId = conversationRepository.getTotalConversationsCount() + 1;
                    boolean updateSuccess = conversationRepository.createConversation(conversationId, username, question, answer);
                    if (!updateSuccess) {
                        System.out.println("Error updating conversation");
                    }

                    // 构建响应 JSON
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);
                    responseJson.put("question", question);
                    responseJson.put("answer", answer);

                    // 发送响应
                    sendResponse(exchange, 200, responseJson.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "Internal Server Error");
                }
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
