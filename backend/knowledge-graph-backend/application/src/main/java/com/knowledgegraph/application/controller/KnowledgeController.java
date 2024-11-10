package com.knowledgegraph.application.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.knowledgegraph.application.service.KnowledgeService;
import com.knowledgegraph.application.service.KnowledgeServiceImpl;
import com.knowledgegraph.application.model.KnowledgePoint;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class KnowledgeController {

    private static KnowledgeService knowledgeService = new KnowledgeServiceImpl();

    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/knowledge/search", new SearchHandler());
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("keyword=")) {
                    String keyword = query.split("=")[1];

                    List<KnowledgePoint> results = knowledgeService.searchKnowledgePoints(keyword);

                    // 构建 JSON 响应
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);

                    JSONArray resultArray = new JSONArray();
                    for (KnowledgePoint kp : results) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", kp.getId());
                        jsonObject.put("name", kp.getName());
                        jsonObject.put("introduction", kp.getIntroduction());
                        resultArray.put(jsonObject);
                    }
                    responseJson.put("result", resultArray);

                    String response = responseJson.toString();
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                } else {
                    exchange.sendResponseHeaders(400, -1); // Bad Request
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}
