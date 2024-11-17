package com.knowledgegraph.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgegraph.application.model.GradingResult;
import com.knowledgegraph.application.model.KnowledgePoint;
import com.knowledgegraph.application.service.SearchService;
import com.knowledgegraph.application.service.SearchServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
    public static void registerEndpoints(HttpServer server) {
        server.createContext("/api/exam", new ExamCorrectingHandler());
        //server.createContext("/api/exam");
    }

    static class ExamCorrectingHandler implements HttpHandler {
        // 提交试卷答案并批改
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            // 获取请求方法
            String requestMethod = exchange.getRequestMethod();
            // 获取请求路径
            String requestPath = exchange.getRequestURI().getPath();
            // 获取请求参数
            String requestParams = exchange.getRequestURI().getQuery();
            // 构造响应内容
            String response = "请求方法：" + requestMethod + "\n"
                    + "请求路径：" + requestPath + "\n"
                    + "请求参数：" + requestParams;
            System.out.println(response);

            // 获取请求体内容
            InputStream inputStream = exchange.getRequestBody();
            String requestBody = readInputStream(inputStream);
            // 将请求体解析为 Map
            Map<String, String> answers = parseRequestBody(requestBody);
            //System.out.println(answers);

            //获取试卷Id
            Pattern pattern = Pattern.compile("/api/exam/(\\d+)/");
            Matcher matcher = pattern.matcher(requestPath);
            matcher.find();
            String examId = matcher.group(1);
            //System.out.println(examId);

            GradingResult res=new GradingResult();

            boolean flag=true;

            // 处理 POST 请求
            if ("POST".equalsIgnoreCase(requestMethod)) {
                if (requestPath.endsWith("/submit")) {
                    res=examCorrectingController.submitExam(examId,answers);

                } else if (requestPath.endsWith("/grade")) {
                    res=examCorrectingController.gradeWithAI(examId,answers);
                } else {
                    flag=false;
                    exchange.sendResponseHeaders(400, -1); // 错误的请求
                }

                if(flag)
                {
                    //res.print();
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);
                    responseJson.put("totalScore",res.getTotalScore());

                    Map<String, Integer> scores=res.getScores();
                    Map<String, String> feedbacks=res.getFeedback();

                    JSONArray resultArray = new JSONArray();
                    for (String name : scores.keySet()) {
                        Integer score = scores.get(name);
                        String feedback=feedbacks.get(name);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("score", score);
                        jsonObject.put("feedback", feedback);
                        resultArray.put(jsonObject);
                    }
                    responseJson.put("correctionResults", resultArray);

                    String result = responseJson.toString();

                    //System.out.println(result);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, result.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(result.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }

            } else if ("GET".equalsIgnoreCase(requestMethod)) {
                if (requestPath.endsWith("/review")) {
                    res=examCorrectingController.reviewExam(examId,answers);
                } else {
                    flag=false;
                    exchange.sendResponseHeaders(400, -1); // 错误的请求
                }

                if(flag)
                {
                    //res.print();
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);

                    Map<String, String> feedbacks=res.getFeedback();

                    JSONArray resultArray = new JSONArray();
                    for (String name : feedbacks.keySet()) {
                        String feedback=feedbacks.get(name);
                        resultArray.put(feedback);
                    }
                    responseJson.put("feedback", resultArray);

                    String result = responseJson.toString();
                    //System.out.println(result);

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, result.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(result.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }

            } else {
                flag=false;
                exchange.sendResponseHeaders(405, -1); // 不支持的请求方法
            }


        }
        // 读取输入流并转换为字符串
        private String readInputStream(InputStream inputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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

            // 将 JSON 数组转换为一个 List<Map<String, String>>
            List<Map<String, String>> answersList = objectMapper.readValue(requestBody, List.class);

            // 创建一个 Map 来存储解析后的结果
            Map<String, String> answersMap = new HashMap<>();

            // 将每个 id 和 userAnswer 对应起来
            for (Map<String, String> answer : answersList) {
                String id = answer.get("id");
                String userAnswer = answer.get("userAnswer");
                answersMap.put(id, userAnswer);
            }

            return answersMap;
        }
    }
}

