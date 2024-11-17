package com.knowledgegraph.application.controller;

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

public class ExamController {

    private static final ExamService examService = new ExamServiceImpl();

    public static void registerEndpoints(HttpServer server) {
        // 注册生成试卷的接口
        server.createContext("/api/exam/generate", new GenerateExamHandler());
        // 注册保存试卷的接口
        server.createContext("/api/exam/save", new SaveExamHandler());
        // 注册获取试卷的接口
        server.createContext("/api/exam/getExam", new GetExamHandler());
    }

    // 生成试卷接口的处理器
    static class GenerateExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    byte[] body = is.readAllBytes();
                    String requestBody = new String(body, StandardCharsets.UTF_8);
                    System.out.println("收到的生成试卷请求: " + requestBody);

                    JSONArray knowledgeIds = new JSONArray(requestBody);
                    JSONObject responseJson = examService.generateExam(knowledgeIds);

                    System.out.println("生成的试卷响应: " + responseJson.toString());
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
                exchange.sendResponseHeaders(405, -1); // 方法不允许
            }
        }
    }

    // 保存试卷接口的处理器
    static class SaveExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=")) {
                    String examId = query.split("=")[1];
                    System.out.println("收到的保存试卷请求 ID: " + examId);

                    JSONObject responseJson = examService.saveExam(examId);

                    System.out.println("保存试卷响应: " + responseJson.toString());
                    String response = responseJson.toString();
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    exchange.sendResponseHeaders(400, -1); // 请求错误
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // 方法不允许
            }
        }
    }

    //获取试卷接口的处理器
    static class GetExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String examId = null;

                // 解析参数
                if (query != null && query.startsWith("examId=")) {
                    examId = query.split("=")[1];
                }

                // 调用服务获取试卷
                JSONArray responseJson = examService.getExam(examId);

                String response = responseJson.toString();
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // 方法不允许
            }
        }
    }
}
