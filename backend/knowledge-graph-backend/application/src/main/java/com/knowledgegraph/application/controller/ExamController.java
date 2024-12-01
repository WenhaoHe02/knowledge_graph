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
        server.createContext("/api/exam/generate", new CORSHandler(new GenerateExamHandler()));
        server.createContext("/api/exam/getExam", new CORSHandler(new GetExamHandler()));
        server.createContext("/api/exam/submit", new CORSHandler(new SubmitExamHandler()));
        server.createContext("/api/exam/getAnswer", new CORSHandler(new GetAnswersByExamIdHandler()));
        server.createContext("/api/exam/save", new CORSHandler(new SaveExamHandler())); // 保留接口
        server.createContext("/api/exam/del", new CORSHandler(new DelExamHandler()));   // 保留接口
    }

    static class CORSHandler implements HttpHandler {
        private final HttpHandler next;

        public CORSHandler(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
            } else {
                next.handle(exchange);
            }
        }
    }

    // 生成试卷接口
    static class GenerateExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject requestJson = new JSONObject(requestBody);

                    String username = requestJson.getString("username");
                    JSONArray knowledgeIds = requestJson.getJSONArray("knowledgeIds");

                    JSONObject responseJson = examService.generateExam(knowledgeIds, username);

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
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // 获取试卷接口
    static class GetExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String examId = null, username = null;

                    if (query != null) {
                        for (String param : query.split("&")) {
                            String[] keyValue = param.split("=");
                            if (keyValue.length == 2) { // 确保键值对格式正确
                                if (keyValue[0].equals("examId")) {
                                    examId = keyValue[1]; // 如果 examId 存在，获取其值
                                } else if (keyValue[0].equals("username")) {
                                    username = keyValue[1]; // 获取用户名
                                }
                            }
                        }
                    }

                    if (username == null || username.isEmpty()) {
                        // 如果 username 为空，抛出异常
                        throw new IllegalArgumentException("缺少必要的参数 username");
                    }

// 调用服务方法获取试卷
                    JSONArray responseJson;
                    if (examId != null && !examId.isEmpty()) {
                        // 如果 examId 存在，则获取指定试卷
                        responseJson = examService.getExam(examId, username);
                    } else {
                        // 如果 examId 不存在，则获取该用户的所有试卷
                        responseJson = examService.getExam(null, username);
                    }


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
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // 提交试卷接口
    static class SubmitExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject requestJson = new JSONObject(requestBody);

                    String examId = requestJson.getString("examId");
                    JSONArray answers = requestJson.getJSONArray("answers");
                    String username = requestJson.getString("username");

                    boolean isSubmitted = examService.submitExam(examId, answers, username);

                    JSONObject responseJson = new JSONObject();
                    responseJson.put("code", isSubmitted ? 200 : 500);

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
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // 获取用户作答接口
    static class GetAnswersByExamIdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String examId = null, username = null;

                    if (query != null) {
                        for (String param : query.split("&")) {
                            String[] keyValue = param.split("=");
                            if (keyValue.length == 2) { // 确保键值对格式正确
                                if (keyValue[0].equals("examId")) {
                                    examId = keyValue[1]; // 如果 examId 存在，获取其值
                                } else if (keyValue[0].equals("username")) {
                                    username = keyValue[1]; // 获取用户名
                                }
                            }
                        }
                    }

                    if (username == null || username.isEmpty()) {
                        // 如果 username 为空，抛出异常
                        throw new IllegalArgumentException("缺少必要的参数 username");
                    }

// 调用服务方法获取试卷
                    JSONArray userAnswers;
                    if (examId != null && !examId.isEmpty()) {
                        // 如果 examId 存在，则获取指定试卷
                        userAnswers = examService.getAnswersByExamId(examId, username);
                    } else {
                        // 如果 examId 不存在，则获取该用户的所有试卷
                        userAnswers = examService.getAnswersByExamId(null, username);
                    }

                    JSONObject responseJson = new JSONObject();
                    responseJson.put("code", 200);
                    responseJson.put("userAnswers", userAnswers);

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
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // 保留的接口：保存试卷
    static class SaveExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=")) {
                    String examId = query.split("=")[1];
                    JSONObject responseJson = examService.saveExam(examId);

                    String response = responseJson.toString();
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // 保留的接口：删除试卷
    static class DelExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject requestJson = new JSONObject(requestBody);

                    if (!requestJson.has("examId")) {
                        throw new IllegalArgumentException("缺少必要的参数 examId");
                    }

                    String examId = requestJson.getString("examId");
                    boolean isDeleted = examService.deleteExam(examId);

                    JSONObject responseJson = new JSONObject();
                    responseJson.put("code", isDeleted ? 200 : 404);

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
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}