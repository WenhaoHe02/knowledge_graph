package com.knowledgegraph.application.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import com.knowledgegraph.application.model.Relation;
import com.knowledgegraph.application.repository.RelationRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RelationExportController {

    private static final RelationRepository relationRepository = new RelationRepository();  // 创建 RelationRepository 实例

    // 注册端点
    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/order_relation", new RelationHandler());
    }

    static class RelationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    // 调用 Repository 获取所有关系数据
                    List<Relation> relations = relationRepository.findAllRelations();
                    sendResponse(exchange, 200, true, relations);

                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, false, null);
                }
            } else {
                sendResponse(exchange, 405, false, null);
            }
        }

        // 发送响应
        private void sendResponse(HttpExchange exchange, int statusCode, boolean success, List<Relation> relations) throws IOException {
            JSONObject responseJson = new JSONObject();
            responseJson.put("code", statusCode);
            responseJson.put("owner", 2); // 假设是某个拥有者组号
            responseJson.put("success", success);

            if (relations != null) {
                JSONArray resultArray = new JSONArray();
                for (Relation relation : relations) {
                    JSONObject relationJson = new JSONObject();
                    relationJson.put("pointA", relation.getPointA());
                    relationJson.put("pointB", relation.getPointB());
                    relationJson.put("relation", relation.getRelation());
                    resultArray.put(relationJson);
                }
                responseJson.put("result", resultArray);
            } else {
                responseJson.put("result", new JSONArray());
            }

            String response = responseJson.toString();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
