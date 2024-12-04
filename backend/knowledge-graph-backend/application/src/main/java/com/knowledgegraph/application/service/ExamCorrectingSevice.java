package com.knowledgegraph.application.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Flowable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

import com.knowledgegraph.application.model.GradingResult;

public class ExamCorrectingSevice {
    private ExamCorrectingSevice() {
        // private constructor to prevent instantiation
    }

    //private static final Dotenv dotenv = Dotenv.configure().directory("src/main/java/com/knowledgegraph/application/service/.env").load();
    private static final String API_SECRET_KEY = "11b0395a1c16c28ac50fd960887e680a.wucr1Z7sxKo6wvM6"; // 请确保安全性，避免硬编码

    private static String json_example =
            """
            {
              "scores": {
                "question1": 10,
                "question2": 15
              },
              "feedback": {
                "question1": "Well done!",
                "question2": "Needs improvement"
              },
              "totalScore": 25
            }                   
            """;

    private static String quesList =
            """
            "quesList": [
                {
                  "titleContent": "string",
                  "standardAnswer": "string"
                }
              ]  
            """;

    private static String ansList =
            """
            [
                {
                    "id": "string",
                    "userAnswer": "string"
                }
            ]     
            """;

    private static String __retriever_prompt =
            "你现在扮演试卷批改的角色。根据以下提供的问题和参考答案，以及用户的回答，请对试卷进行批改。试题类型包括单项选择题、判断题和简答题。单项选择题的参考答案为 A、B、C 或 D，判断题的参考答案为 T 或 F。\n" +
                    "我将以以下格式提供问题和参考答案：\n" +
                    "quesList: [\n" +
                    "    {\n" +
                    "      \"titleContent\": \"问题描述\",\n" +
                    "      \"standardAnswer\": \"参考答案\"\n" +
                    "    }\n" +
                    "  ]\n\n" +
                    "用户的作答以以下格式提供：\n" +
                    "answers: [\n" +
                    "    {\n" +
                    "        \"id\": \"问题编号\",\n" +
                    "        \"userAnswer\": \"用户回答\"\n" +
                    "    }\n" +
                    "]\n\n" +
                    "请根据以上内容，按照以下 JSON 格式返回批改结果，不要包含任何额外的评论或文本：\n" +
                    "{\n" +
                    "  \"scores\": {\n" +
                    "    \"question1\": 10,\n" +
                    "    \"question2\": 15\n" +
                    "  },\n" +
                    "  \"feedback\": {\n" +
                    "    \"question1\": \"反馈信息\",\n" +
                    "    \"question2\": \"反馈信息\"\n" +
                    "  },\n" +
                    "  \"totalScore\": 25\n" +
                    "}\n\n" +
                    "请用中文回答。";

    private static String getText(Map<String, String> quesListMap, Map<String, String> answers) throws JsonProcessingException {
        // 创建ObjectMapper实例
        ObjectMapper objectMapper = new ObjectMapper();

        // 将对象转换为JSON字符串
        String jsonQuesString = "quesList: " + objectMapper.writeValueAsString(quesListMap);
        String jsonAnsString = "answers: " + objectMapper.writeValueAsString(answers);

        return __retriever_prompt + "\n" + jsonQuesString + "\n" + jsonAnsString;
    }


    private static final ClientV4 client = new ClientV4.Builder(API_SECRET_KEY)
            .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
            .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
            .build();

    public static Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ModelData> flowable) {
        return flowable.map(chunk -> {
            return new ChatMessageAccumulator(chunk.getChoices().get(0).getDelta(), null, chunk.getChoices().get(0), chunk.getUsage(), chunk.getCreated(), chunk.getId());
        });
    }

    private static String extractJson(String input) {
        // 寻找第一个 '{' 和最后一个 '}' 之间的内容
        int firstBrace = input.indexOf('{');
        int lastBrace = input.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return input.substring(firstBrace, lastBrace + 1);
        }
        return null;
    }

    private static String getResponse(String text) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "你现在扮演试卷批改的角色，要求根据用户输入和AI的回答，正确提取出信息。"));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), text));

        StringBuilder responseBuilder = new StringBuilder();

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .messages(messages)
                .tools(new ArrayList<>())
                .toolChoice("auto")
                .build();

        ModelApiResponse sseModelApiResp = client.invokeModelApi(chatCompletionRequest);
        if (sseModelApiResp.isSuccess()) {
            ChatMessageAccumulator chatMessageAccumulator = mapStreamToAccumulator(sseModelApiResp.getFlowable())
                    .doOnNext(accumulator -> {
                        if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
                            responseBuilder.append(accumulator.getDelta().getContent());
                            System.out.print(accumulator.getDelta().getContent());
                        }
                    })
                    .lastElement()
                    .blockingGet();

            String accumulatedResponse = responseBuilder.toString();
            System.out.println("\nAccumulated response: " + accumulatedResponse);
            String res = extractJson(accumulatedResponse);
            System.out.println("Extracted JSON: " + res);
            return res;
        }
        return null;
    }

    public static GradingResult Correcting(Map<String, String> quesList, Map<String, String> answers) throws JsonProcessingException {
        System.out.println("Constructed Prompt:\n" + getText(quesList, answers));
        String res = getResponse(getText(quesList, answers));
        int retryCount = 0;
        int maxRetries = 3;
        while (res == null && retryCount < maxRetries) {
            System.out.println("Extraction failed, retrying... Attempt " + (retryCount + 1));
            res = getResponse(getText(quesList, answers));
            retryCount++;
        }
        if (res == null) {
            throw new RuntimeException("Failed to extract grading results from AI response.");
        }
        // 创建Gson实例
        Gson gson = new Gson();

        // 将JSON字符串转换为GradingResult对象
        GradingResult gradRes = gson.fromJson(res, GradingResult.class);
        return gradRes;
    }

}
