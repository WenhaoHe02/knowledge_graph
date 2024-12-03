package com.knowledgegraph.application.service;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class QAServiceImpl implements QAService {

    // 使用 Dotenv 加载 .env 文件中的配置


    private static final String PY_ENVIRONMENT ="local";
    private static final boolean PY_DEBUG = true;
    private static final String LLM_BASE_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String LLM_API_SECRET_KEY = "11b0395a1c16c28ac50fd960887e680a.wucr1Z7sxKo6wvM6";
    private static final String MODEL_NAME = "glm-4";
    private static final String ORGANIZATION_NAME ="奶龙大军";

    // 调用 LLM API，获取 AI 回答
    public static String callLLMApi(String userInput) {
        OkHttpClient client = new OkHttpClient();

        // 构建请求体
        JSONObject payload = new JSONObject();
        payload.put("model", MODEL_NAME); // 检查是否需要修改模型名称
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", userInput));
        payload.put("messages", messages);
        payload.put("max_tokens", 500);

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