package com.knowledgegraph.application.controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgegraph.application.model.Exam;
import com.knowledgegraph.application.model.GradingResult;
import com.knowledgegraph.application.repository.ExamCorrectingReposity;
import com.knowledgegraph.application.service.ExamCorrectingSevice;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import static org.springframework.core.io.buffer.DataBufferUtils.readInputStream;

public class ExamCorrectingController  {

    public GradingResult submitExam( String examId,  Map<String, String> answers) throws JsonProcessingException {
        // 调用大模型批改功能
        GradingResult result = gradeWithAI(examId, answers);
        return result;
    }

    // 批改试卷（后台调用）
    public GradingResult gradeWithAI( String examId,  Map<String, String> answers) throws JsonProcessingException {
        Exam exam = getExam(examId);
        Map<String, String> quesList = exam.answers;
        //System.out.println(quesList);
        return ExamCorrectingSevice.Correcting(quesList, answers);
    }

    // 教师对试卷进行二次批改或复核
    public GradingResult reviewExam(String examId, Map<String, String> feedback) {
        GradingResult result = new GradingResult(); // 返回一个批改结果

        // 教师修改批改结果
        for (String name : feedback.keySet()) {
            String feed=feedback.get(name);
            //System.out.println(name);
            //System.out.println(feed);
            result.addFeedback(name, feed);
        }

        return result;
    }

    private static Exam getExam(String examId)
    {
        ExamCorrectingReposity exRe=new ExamCorrectingReposity();
        return exRe.searchExam(examId);
    }
}
