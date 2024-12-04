package com.knowledgegraph.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgegraph.application.model.*;
import com.knowledgegraph.application.repository.ExamCorrectingReposity;
import com.knowledgegraph.application.service.SearchService;
import com.knowledgegraph.application.service.SearchServiceImpl;
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
        // 提交试卷答案并批改
        @Override
        public void handle(HttpExchange exchange) throws IOException {
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

            // 获取请求体内容
            InputStream inputStream = exchange.getRequestBody();
            String requestBody = readInputStream(inputStream);

            System.out.println(requestParams);

            //获取试卷Id或用户答案id
            Pattern pattern = Pattern.compile("/api/exam/([^/]+)/");
            Matcher matcher = pattern.matcher(requestPath);
            matcher.find();
            String examId = matcher.group(1);
            System.out.println(examId);
            GradingResult res=new GradingResult();

            boolean flag=true;

            // 处理 POST 请求
            if ("POST".equalsIgnoreCase(requestMethod)) {
                // 将请求体解析为 Map
                Map<String, String> answers = parseRequestBody(requestBody);
                String[] UserAnsId = requestParams.split("userAnsId=|&username=");
                if (requestPath.endsWith("/submit")) {
                    examCorrectingReposity.addUserAns(UserAnsId[1],examId,answers);
                    res=examCorrectingController.submitExam(examId,answers);
                    examCorrectingReposity.addCorrectingRes(UserAnsId[1],res,UserAnsId[2]);
                } else if (requestPath.endsWith("/grade")) {
                    examCorrectingReposity.addUserAns(UserAnsId[1],examId,answers);
                    res=examCorrectingController.gradeWithAI(examId,answers);
                    res.print();
                    System.out.println("112223");
                    examCorrectingReposity.addCorrectingRes(UserAnsId[1],res,UserAnsId[2]);
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
                System.out.println(requestBody);
                res=parseRequestBody2(requestBody);
                if (requestPath.endsWith("/review")) {
                    String[] UserAnsId = requestParams.split("username=");
                    //res=examCorrectingController.reviewExam(examId,gradRes);
                    examCorrectingReposity.addCorrectingRes(examId,res,UserAnsId[1]);
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
                    System.out.println(result);

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

        // 将请求体解析为 GradingRes（假设是 JSON 格式）
        private GradingResult parseRequestBody2(String requestBody) throws IOException {
            ObjectMapper objectMapper = new ObjectMapper();
            // 将 JSON 数组转换为一个 List<Map<String, String>>
            Request answersList = objectMapper.readValue(requestBody, Request.class);
            // 创建一个 GradingResult 来存储解析后的结果
            GradingResult res =new GradingResult();
            int i=1;
            for(CorrectionResult cor:answersList.getCorrectionResults())
            {
                System.out.println(cor);
                res.addScore(String.valueOf(i),cor.getScore());
                res.addFeedback(String.valueOf(i),cor.getFeedback());
                ++i;
            }
            // 输出结果，确认转换成功
            //System.out.println("Scores: " + res.getScores());
            //System.out.println("Feedback: " + res.getFeedback());
            //System.out.println("Total Score: " + res.getTotalScore());
            return res;
        }
    }
}

