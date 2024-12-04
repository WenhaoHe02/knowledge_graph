package com.knowledgegraph.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgegraph.application.model.*;
import com.knowledgegraph.application.repository.ExamCorrectingReposity;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.ToString;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExamCorrectingHttpController {

    public static ExamCorrectingController examCorrectingController=new ExamCorrectingController();
    public static ExamCorrectingReposity examCorrectingReposity=new ExamCorrectingReposity();
    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/exam", new ExamCorrectingHandler());
        //server.createContext("/api/exam");
    }
    static class ExamCorrectingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 设置CORS头
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");

            // 处理OPTIONS预检请求
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestURI().getPath();
            String requestParams = exchange.getRequestURI().getQuery();

            // 分割路径
            String[] parts = requestPath.split("/");
            if (parts.length < 5) {
                exchange.sendResponseHeaders(400, -1); // 错误的路径
                return;
            }

            String examId = parts[3];
            String action = parts[4]; // "grade" 或 "submit"

            // 解析查询参数
            Map<String, String> queryParams = parseQueryParams(requestParams);
            String userAnsId = queryParams.get("userAnsId");
            String username = queryParams.get("username");

            if (userAnsId == null || username == null) {
                exchange.sendResponseHeaders(400, -1); // 缺少参数
                return;
            }

            // 获取请求体内容
            InputStream inputStream = exchange.getRequestBody();
            String requestBody = readInputStream(inputStream);

            GradingResult res = new GradingResult();
            boolean flag = true;

            try {
                if ("POST".equalsIgnoreCase(requestMethod)) {
                    // 将请求体解析为 Map
                    Map<String, String> answers = parseRequestBody(requestBody);

                    if ("submit".equalsIgnoreCase(action)) {
                        examCorrectingReposity.addUserAns(userAnsId, examId, answers);
                        res = examCorrectingController.submitExam(examId, answers);
                        examCorrectingReposity.addCorrectingRes(userAnsId, res, username);
                    } else if ("grade".equalsIgnoreCase(action)) {
                        examCorrectingReposity.addUserAns(userAnsId, examId, answers);
                        res = examCorrectingController.gradeWithAI(examId, answers);
                        examCorrectingReposity.addCorrectingRes(userAnsId, res, username);
                    } else {
                        flag = false;
                        exchange.sendResponseHeaders(400, -1); // 不支持的动作
                    }

                    if (flag) {
                        // 构建响应 JSON
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("success", true);
                        responseJson.put("code", 200);
                        responseJson.put("totalScore", res.getTotalScore());

                        Map<String, Integer> scores = res.getScores();
                        Map<String, String> feedbacks = res.getFeedback();

                        JSONArray resultArray = new JSONArray();
                        for (String key : scores.keySet()) {
                            Integer score = scores.get(key);
                            String feedback = feedbacks.get(key);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("score", score);
                            jsonObject.put("feedback", feedback);
                            resultArray.put(jsonObject);
                        }
                        responseJson.put("correctionResults", resultArray);

                        String result = responseJson.toString();

                        // 设置响应头和发送响应
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, result.getBytes(StandardCharsets.UTF_8).length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(result.getBytes(StandardCharsets.UTF_8));
                        os.close();
                    }
                } else if ("GET".equalsIgnoreCase(requestMethod)) {
                    // 处理GET请求（如 /review）
                    // 根据您的业务逻辑实现
                    // 这里暂不详细展开
                    exchange.sendResponseHeaders(501, -1); // 未实现
                } else {
                    exchange.sendResponseHeaders(405, -1); // 不支持的请求方法
                }
            } catch (Exception e) {
                // 处理异常，返回500错误
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
        }

        // 读取输入流并转换为字符串
        private String readInputStream(InputStream inputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }

        // 将请求体解析为 Map（假设是 JSON 格式）
        private Map<String, String> parseRequestBody(String requestBody) throws IOException {
            ObjectMapper objectMapper = new ObjectMapper();

            // 将 JSON数组转换为 List<Map<String, String>>
            List<Map<String, String>> answersList = objectMapper.readValue(requestBody, List.class);

            // 创建一个 Map 来存储解析后的结果
            Map<String, String> answersMap = new HashMap<>();

            // 将每个 id 和 userAnswer 对应起来
            for (Map<String, String> answer : answersList) {
                String id = answer.get("id");
                String userAnswer = answer.get("userAnswer");
                if (id != null && userAnswer != null) {
                    answersMap.put(id, userAnswer);
                }
            }

            return answersMap;
        }

        // 将请求体解析为 GradingResult（假设是 JSON 格式）
        private GradingResult parseRequestBody2(String requestBody) throws IOException {
            ObjectMapper objectMapper = new ObjectMapper();
            // 将 JSON 转换为 GradingResult 对象
            // 假设您的 GradingResult 类和 JSON 结构匹配
            GradingResult res = objectMapper.readValue(requestBody, GradingResult.class);
            return res;
        }

        // 解析查询参数
        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query == null || query.isEmpty()) {
                return params;
            }
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
            return params;
        }
    }

}

