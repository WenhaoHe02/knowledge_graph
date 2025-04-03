package com.knowledgegraph.application.service;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class QAServiceImpl implements QAService {

    private static final String LLM_BASE_URL = "https://api.deepseek.com/chat/completions";
    private static final String LLM_API_SECRET_KEY = "sk-3534578723244677b72ad1a3da2f13bd";
    private static final String MODEL_NAME = "deepseek-chat";

    // 调用 LLM API，获取 AI 回答
    public static String callLLMApi(String userInput) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)  // 设置连接超时
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)     // 设置读取超时
                .build();

        // 构建请求体
        JSONObject payload = new JSONObject();
        payload.put("model", MODEL_NAME);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", userInput));
        payload.put("messages", messages);
        payload.put("max_tokens", 10000);

        // 构建请求
        RequestBody body = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"), payload.toString());
        Request request = new Request.Builder()
                .url(LLM_BASE_URL)
                .addHeader("Authorization", "Bearer " + LLM_API_SECRET_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                return jsonResponse.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content");
            } else {
                // 打印详细错误信息
                System.err.println("Error: " + response.code() + " - " + response.message());
                if (response.body() != null) {
                    System.err.println("Error Response Body: " + response.body().string());
                }
                return "Error occurred while calling the API.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: " + e.getMessage();
        }
    }

    @Override
    public String getAnswer(String question) {
        System.out.println("Calling LLM API for question: " + question);
        // 调用 LLM API 获取 AI 回答
        String answer = callLLMApi(question);
        return answer;
    }
}
