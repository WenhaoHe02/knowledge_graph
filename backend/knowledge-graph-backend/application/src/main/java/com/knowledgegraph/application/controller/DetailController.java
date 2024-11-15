package com.knowledgegraph.application.controller;

import com.knowledgegraph.application.model.KnowledgePoint;
import com.knowledgegraph.application.model.DetailKnowledgePoint;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.knowledgegraph.application.service.DetailService;
import com.knowledgegraph.application.service.DetailServiceImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetailController {
    private static DetailService detailService = new DetailServiceImpl();

    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/knowledge/detail", new DetailHandle());
    }

    static class DetailHandle implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=")) {
                    String id = query.split("=")[1];
                    id = (String) (id.subSequence(1, id.length() - 1));
                    System.out.println("id: " + id);

                    DetailKnowledgePoint point = detailService.searchDetailKnowledgePoints(id);

                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);

                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("id", point.getId());
                    jsonObject.put("name", point.getName());
                    jsonObject.put("content", point.getContent());
                    jsonObject.put("cognition", point.getCognition());
                    jsonObject.put("tag", point.getTag());
                    jsonObject.put("note", point.getNote());

                    JSONArray prePointArray = new JSONArray();
                    for (String preId : point.getPrePoint()) {
                        prePointArray.put(preId);
                    }
                    jsonObject.put("prePoint", prePointArray);

                    JSONArray postPointArray = new JSONArray();
                    for (String postId : point.getPostPoint()) {
                        postPointArray.put(postId);
                    }
                    jsonObject.put("postPoint", postPointArray);

                    // 处理 relatedPoints 数组
                    JSONArray relatedPointsArray = new JSONArray();
                    for (String relatedId : point.getRelatedPoint()) {
                        relatedPointsArray.put(relatedId);
                    }
                    jsonObject.put("relatedPoint", relatedPointsArray);

                    responseJson.put("result", jsonObject);
                    System.out.println(responseJson);
                    String response = responseJson.toString();

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                } else {
                    System.out.println("参数不符合规定");
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                System.out.println("方法不被允许: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, -1);
            }
        }

    }
}
