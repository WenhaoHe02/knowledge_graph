package com.knowledgegraph.application.controller;

import com.knowledgegraph.application.model.QuesList;
import com.knowledgegraph.application.service.ExamService;
import com.knowledgegraph.application.service.ExamServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExamController {
    private static ExamService examService = new ExamServiceImpl();

    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/exam/generate", new GenerateExamHandler());
    }

    static class GenerateExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    // 读取请求 Body
                    byte[] body = is.readAllBytes();
                    String requestBody = new String(body, StandardCharsets.UTF_8);

                    // 打印接收到的请求 Body
                    System.out.println("收到的请求信息: " + requestBody);

                    JSONArray knowledgeIds = new JSONArray(requestBody);

                    // 调用服务生成试卷
                    JSONObject responseJson = examService.generateExam(knowledgeIds);

                    // 打印传递的响应信息
                    System.out.println("生成的响应信息: " + responseJson.toString());

                    // 返回响应
                    String response = responseJson.toString();
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                }
            } else {
                System.out.println("方法不允许: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, -1); // 方法不允许
            }
        }
    }
}
