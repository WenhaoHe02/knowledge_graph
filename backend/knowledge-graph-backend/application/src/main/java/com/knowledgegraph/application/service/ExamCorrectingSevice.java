package com.knowledgegraph.application.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.Flowable;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;


import com.knowledgegraph.application.model.Exam;
import com.knowledgegraph.application.model.GradingResult;

public class ExamCorrectingSevice {
    private ExamCorrectingSevice() {
        // private constructor to prevent instantiation
    }

    //private static final Dotenv dotenv = Dotenv.configure().directory("src/main/java/com/knowledgegraph/application/service/.env").load();
    private static final String API_SECRET_KEY = "11b0395a1c16c28ac50fd960887e680a.wucr1Z7sxKo6wvM6";//.env文件中

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

    private static String quesList=
            """
            "quesList": [
                {
                  "titleContent": "string",
                  "standardAnswer": "string"
                }
              ]  
            """;

    private static String ansList=
            """
            [
                {
                    "id": "string",
                    "userAnswer": "string"
                }
            ]     
            """;

    private static String __retriever_prompt =
            "You are an AI expert specializing in grading exam questions for software project management."+
                    "Based on the given questions and reference answers, as well as user provided answers, you will grade and grade the exam questions." +
                    "The types of questions include single-choice questions, true/false questions, and short answer questions. "+
                    "The reference answers for single-choice questions are A, B, C, or D, while the reference answers for true or false questions are T or F. "+
                    "I will provide a list of questions and reference answers in the following format"+quesList+
                    "user input answers in the following format"+ansList+
                    "please grade the exam questions based on these and provide the grading results in the following format\n"+
                    "'''\n"+json_example+"\n'''"+
                    "\nplease answer in Chinese and provide detailed and comprehensive relationships in your reply.\n"+
                    "Please provide the correction results in accordance with this format,without any unnecessary comments or text\n"+
                    "Please use '' and '' '' to identify the correction results in JSON format, for example\n"+
                    "'''\n"+
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
                    """+
                    "'''\n";


    private static String getText(Map<String, String> quesList, Map<String, String> answers) throws JsonProcessingException {
        // 创建ObjectMapper实例
        ObjectMapper objectMapper = new ObjectMapper();

        // 将对象转换为JSON字符串
        String jsonQuesString = "List of questions and reference answers：\n"+objectMapper.writeValueAsString(quesList);
        String jsonAnsString = "The answer entered by the user:\n"+objectMapper.writeValueAsString(answers);

        return __retriever_prompt+jsonQuesString+'\n'+jsonAnsString;
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

    public static String extractTextBetweenBackticks(String input) {
        // 正则表达式匹配三个反引号之间的文本
        System.out.println(input);
        Pattern pattern = Pattern.compile("'''(.*?)'''", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // 提取文本并去掉 "json"
            String jsonString = matcher.group(1).trim();
            return jsonString.replace("json", "").trim(); // 去掉 "json" 字符串
        }
        else{
            pattern = Pattern.compile("```(.*?)```", Pattern.DOTALL);
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                // 提取文本并去掉 "json"
                String jsonString = matcher.group(1).trim();
                return jsonString.replace("json", "").trim(); // 去掉 "json" 字符串
            }
            else
                return null;
        }
    }

    private static String getResponse(String text) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "你现在扮演试卷批改的角色，要求根据用户输入和AI的回答，正确提取出信息。"));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), text));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), __retriever_prompt));
        String res=new String();



        // 函数调用参数构建部分
        List<ChatTool> chatToolList = new ArrayList<>();
        ChatTool chatTool = new ChatTool();
        chatTool.setType(ChatToolType.FUNCTION.value());
        ChatFunctionParameters chatFunctionParameters = new ChatFunctionParameters();
        chatFunctionParameters.setType("object");

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .messages(messages)
                .tools(chatToolList)
                .toolChoice("auto")
                .build();

        ModelApiResponse sseModelApiResp = client.invokeModelApi(chatCompletionRequest);
        StringBuilder responseBuilder = new StringBuilder();
        if (sseModelApiResp.isSuccess()) {
            AtomicBoolean isFirst = new AtomicBoolean(true);

            ChatMessageAccumulator chatMessageAccumulator = mapStreamToAccumulator(sseModelApiResp.getFlowable())
                    .doOnNext(accumulator -> {
                        {
                            if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
                                responseBuilder.append(accumulator.getDelta().getContent());
                                //System.out.print(accumulator.getDelta().getContent());
                            }
                        }
                    })
                    .lastElement()
                    .blockingGet();

            //System.out.println(responseBuilder.toString());

            res = extractTextBetweenBackticks(responseBuilder.toString());

            //System.out.println(res);
            return res;
        }


        return res;
    }

    public static GradingResult Correcting(Map<String, String> quesList,Map<String, String> answers) throws JsonProcessingException {
        //System.out.println(getText(quesList, answers));
        String res=getResponse(getText(quesList, answers));
        //System.out.println(res);
        while(res==null)
            res=getResponse(getText(quesList, answers));
        // 创建Gson实例
        Gson gson = new Gson();

        // 将JSON字符串转换为Person对象
        GradingResult gradRes = gson.fromJson(res, GradingResult.class);

        return gradRes;
    }

}
