package com.knowledgegraph.application.controller;

import com.knowledgegraph.application.model.ConversationDTO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.knowledgegraph.application.repository.ConversationRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class QAhistoryController {
    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/qa/history", new AskHandler());
    }
    static class AskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许跨域访问
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String queryParams = exchange.getRequestURI().getQuery();
                    if (queryParams == null || queryParams.trim().isEmpty()) {
                        sendResponse(exchange, 400, "Bad Request: No parameters provided.");
                        return;
                    }

                    String username = null;
                    for (String param : queryParams.split("&")) {
                        String[] pair = param.split("=");
                        if (pair.length == 2 && "username".equalsIgnoreCase(pair[0])) {
                            username = pair[1];

                            break;  // Stop once we've found the username, no need to keep parsing
                        }
                    }

                    if (username == null || username.trim().isEmpty()) {
                        sendResponse(exchange, 400, "Bad Request: No username provided.");
                        return;
                    }

                    ConversationRepository a = new ConversationRepository();
                    List<ConversationDTO> conversations = a.getUserConversations(username);
                    System.out.println("shuliang :"+conversations.size());
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);  // 成功标识
                    responseJson.put("code", "200");    // 响应码

                    JSONArray resultArray = new JSONArray();

                    for (ConversationDTO conversation : conversations) {
                        JSONObject conversationJson = new JSONObject();

                        conversationJson.put("id", conversation.getConversationId());
                        System.out.println(conversation.getQuestion());
                        conversationJson.put("question", conversation.getQuestion());
                        conversationJson.put("anwser", conversation.getAnswer());

                        resultArray.put(conversationJson);
                    }

                    responseJson.put("result", resultArray);

                    sendResponse(exchange, 200, responseJson.toString());
                } finally {

                }

                }
            }
        }
    private String getRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // 辅助方法：发送响应
    private static void sendResponse(HttpExchange exchange, int statusCode, String responseMessage) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseMessage.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseMessage.getBytes(StandardCharsets.UTF_8));
        }
    }




    }


