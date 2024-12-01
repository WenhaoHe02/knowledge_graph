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
        // 注册问答接口
        server.createContext("/api/qa/history", new AskHandler());
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

                    // 解析 query 参数（假设请求是类似 ?question=...&conservationid=... 的格式）
                    String username = null;
                    for (String param : queryParams.split("&")) {
                        String[] pair = param.split("=");
                        if (pair.length == 2 && "username".equalsIgnoreCase(pair[0])) {
                            username = pair[1];

                            break;  // Stop once we've found the username, no need to keep parsing
                        }
                    }

                    if (username == null || username.trim().isEmpty()) {
                        // 如果没有找到 username 参数，返回 400 错误
                        sendResponse(exchange, 400, "Bad Request: No username provided.");
                        return;
                    }

                    ConversationRepository a = new ConversationRepository();
                    List<ConversationDTO> conversations = a.getUserConversations(username);
                    System.out.println("shuliang :"+conversations.size());
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);  // 成功标识
                    responseJson.put("code", "200");    // 响应码

                    // 创建 JSON 数组来存储每个对话
                    JSONArray resultArray = new JSONArray();

                    // 遍历所有的对话并格式化为 JSON 结构
                    for (ConversationDTO conversation : conversations) {
                        JSONObject conversationJson = new JSONObject();

                        // 设置对话 id
                        conversationJson.put("id", conversation.getConversationId());
                        System.out.println(conversation.getQuestion());
                        conversationJson.put("question", conversation.getQuestion());
                        conversationJson.put("anwser", conversation.getAnswer());

                        // 将该对话的 JSON 对象加入结果数组
                        resultArray.put(conversationJson);
                    }

                    // 将 result 数组添加到响应 JSON 中
                    responseJson.put("result", resultArray);

                    sendResponse(exchange, 200, responseJson.toString());
                } finally {
                    System.out.println("123123");
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


