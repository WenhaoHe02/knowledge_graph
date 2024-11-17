package com.knowledgegraph.application.controller;

import com.knowledgegraph.application.model.QuesList;
import com.knowledgegraph.application.service.ExerciseService;
import com.knowledgegraph.application.service.ExerciseServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExerciseController {
    private static ExerciseService exerciseService = new ExerciseServiceImpl();

    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/knowledge/exercises", new ExerciseHandle());
    }

    static class ExerciseHandle implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=")) {
                    String id = query.split("=")[1];
                    id = id.substring(1, id.length() - 1); // 解析ID
                    System.out.println("id: " + id);

                    List<QuesList> quesLists = exerciseService.getExercisesByKnowledgePointId(id);

                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);

                    JSONArray quesArray = new JSONArray();
                    for (QuesList ques : quesLists) {
                        JSONObject quesJson = new JSONObject();
                        quesJson.put("titleContent", ques.getTitleContent());
                        quesJson.put("standardAnswer", ques.getStandardAnswer());
                        quesArray.put(quesJson);
                    }
                    responseJson.put("quesList", quesArray);

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
