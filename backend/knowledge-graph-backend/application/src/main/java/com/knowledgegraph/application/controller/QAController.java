package com.knowledgegraph.application.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.knowledgegraph.application.service.QAService;
import com.knowledgegraph.application.service.QAServiceImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class QAController {

    private static QAService qaService = new QAServiceImpl();  // 调用服务层

    public static void registerEndpoints(HttpServer server) {
        // 注册问答接口
        server.createContext("/api/qa/ask", new AskHandler());
    }

    static class AskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许跨域访问
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            System.out.println("111");
            // 只允许 POST 请求
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try (InputStream inputStream = exchange.getRequestBody()) {
                    // 读取请求体
                    String requestBody = exchange.getRequestURI().getQuery();
                    //String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("打引体内容跟"+requestBody); // 打印请求体内容

                    // 如果传入的是字符串而不是 JSON 格式的对象，直接使用 requestBody

                    if (requestBody.isEmpty()) {
                        // 如果没有问题内容，返回 400 错误
                        exchange.sendResponseHeaders(400, -1); // Bad Request
                        System.out.println("111");
                        return;
                    }

                    // 调用服务层处理问题并获取答案
                    String answer = qaService.getAnswer(requestBody);

                    // 构建 JSON 响应
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("success", true);
                    responseJson.put("code", 200);
                    responseJson.put("question", requestBody);
                    responseJson.put("answer", answer);

                    // 将响应写入输出流
                    String response = responseJson.toString();
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    // 处理异常情况，返回 500 错误
                    exchange.sendResponseHeaders(500, -1); // Internal Server Error
                    e.printStackTrace(); // 打印堆栈信息
                }
            } else {
                // 如果请求方法不是 POST，返回 405 错误
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}
