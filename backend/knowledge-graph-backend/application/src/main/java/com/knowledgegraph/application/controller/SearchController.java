package com.knowledgegraph.application.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.knowledgegraph.application.service.SearchService;
import com.knowledgegraph.application.service.SearchServiceImpl;
import com.knowledgegraph.application.model.KnowledgePoint;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class SearchController {

    private static SearchService searchService = new SearchServiceImpl();

    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/knowledge/search", new SearchHandler());
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                System.out.println("查询字符串: " + query);

                if (query != null && query.startsWith("keyword=")) {
                    String keyword = query.split("=")[1];
                    keyword = (String) (keyword.subSequence(1, keyword.length() - 1));
                    System.out.println("Keyword: " + keyword);
                    List<KnowledgePoint> results = searchService.searchKnowledgePoints(keyword);
                    System.out.println("结果数量: " + results.size());

                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);

                    JSONArray resultArray = new JSONArray();
                    for (KnowledgePoint kp : results) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", kp.getId());
                        jsonObject.put("name", kp.getName());
                        jsonObject.put("content", kp.getContent());
                        resultArray.put(jsonObject);
                    }
                    responseJson.put("result", resultArray);

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
