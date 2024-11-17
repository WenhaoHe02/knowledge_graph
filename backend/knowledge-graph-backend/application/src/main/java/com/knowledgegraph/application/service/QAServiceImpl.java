package com.knowledgegraph.application.service;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class QAServiceImpl implements QAService {

    // 使用 Dotenv 加载 .env 文件中的配置
    private static final Dotenv dotenv = Dotenv.load();



    private static final String PY_ENVIRONMENT = dotenv.get("PY_ENVIRONMENT", "local");
    private static final boolean PY_DEBUG = Boolean.parseBoolean(dotenv.get("PY_DEBUG", "false"));
    private static final String LLM_BASE_URL = dotenv.get("LLM_BASE_URL");
    private static final String LLM_API_SECRET_KEY = dotenv.get("LLM_API_SECRET_KEY");
    private static final String MODEL_NAME = dotenv.get("MODEL_NAME");
    private static final String ORGANIZATION_NAME = dotenv.get("ORGANIZATION_NAME", "Default Team");

    // 调用 LLM API，获取 AI 回答
    public static String callLLMApi(String userInput) {
        OkHttpClient client = new OkHttpClient();

        // 构建请求体
        JSONObject payload = new JSONObject();
        payload.put("model", MODEL_NAME);

        // 创建消息数组
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", userInput));
        payload.put("messages", messages);

        // 限制生成的最大 token 数量
        payload.put("max_tokens", 150);

        // 创建请求
        Request request = new Request.Builder()
                .url(LLM_BASE_URL)
                .addHeader("Authorization", "Bearer " + LLM_API_SECRET_KEY)
                .post(RequestBody.create(MediaType.parse("application/json"), payload.toString()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                // 返回 AI 回复内容
                return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            } else {
                System.err.println("Error: " + response.code() + " - " + response.message());
                return "Error occurred while calling the API.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: " + e.getMessage();
        }
    }

    @Override
    public String getAnswer(String question) {
        if (PY_DEBUG) {
            System.out.println("Environment: " + PY_ENVIRONMENT);
            System.out.println("LLM Base URL: " + LLM_BASE_URL);
            System.out.println("Model Name: " + MODEL_NAME);
            System.out.println("Organization Name: " + ORGANIZATION_NAME);
        }

        // 调用 LLM API 获取 AI 回答
        String answer = callLLMApi(question);
        return answer;
    }
}
