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

        GradingResult res=new GradingResult();
        System.out.println("!!!!");
        Exam exam = getExam(examId);
        Map<String, String> quesList = exam.answers;
        //System.out.println(quesList);
        res=ExamCorrectingSevice.Correcting(quesList, answers);
        return res;
    }

    public GradingResult chioceCorrect( Map<String, String> quesList,  Map<String, String> answers)
    {
        GradingResult res=new GradingResult();

        for (String name : quesList.keySet()) {
            String feed=quesList.get(name);
            String ans=answers.get(name);
            String standAns=quesList.get(name);
            if(ans==standAns)
            {
                res.addScore(name,10);
                res.addFeedback(name,"正确");
            }
            else
            {
                res.addScore(name,0);
                res.addFeedback(name,"错误");
            }
        }

        return res;
    }

    public GradingResult judgeCorrect( Map<String, String> quesList,  Map<String, String> answers)
    {
        GradingResult res=new GradingResult();

        for (String name : quesList.keySet()) {
            String feed=quesList.get(name);
            String ans=answers.get(name);
            String standAns=quesList.get(name);
            if(ans==standAns)
            {
                res.addScore(name,10);
                res.addFeedback(name,"正确");
            }
            else
            {
                res.addScore(name,0);
                res.addFeedback(name,"错误");
            }
        }

        return res;
    }

    // 教师对试卷进行二次批改或复核
//    public GradingResult reviewExam(String examId,  GradingResult result) {
//        GradingResult result = new GradingResult(); // 返回一个批改结果
//
//        // 教师修改批改结果
//        for (String name : feedback.keySet()) {
//            String feed=feedback.get(name);
//            //System.out.println(name);
//            //System.out.println(feed);
//            result.addFeedback(name, feed);
//        }
//
//        return result;
//    }

    private static Exam getExam(String examId)
    {
        ExamCorrectingReposity exRe=new ExamCorrectingReposity();
        return exRe.searchExam2(examId);
    }
}
