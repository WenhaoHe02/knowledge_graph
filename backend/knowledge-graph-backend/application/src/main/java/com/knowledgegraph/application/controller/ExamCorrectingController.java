//package com.knowledgegraph.application.controller;
//
//import java.util.Map;
//import java.util.HashMap;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.knowledgegraph.application.model.Exam;
//import com.knowledgegraph.application.model.GradingResult;
//import com.knowledgegraph.application.service.ExamCorrectingSevice;
//
////@RestController
////@RequestMapping("/api/exam")
//public class ExamCorrectingController  {
//
//    private Exam getExam(String examId)
//    {
//        Exam exam=new Exam();
//        exam.getExample();
//        return exam;
//    }
//
//    // 提交试卷答案并批改
//    //@PostMapping("/{examId}/submit")
//    public GradingResult submitExam( String examId,  Map<String, String> answers) throws JsonProcessingException {
//        // 调用大模型批改功能
//        GradingResult result = gradeWithAI(examId, answers);
//        return result;
//    }
//
//    // 批改试卷（后台调用）
//    //@PostMapping("/grade")
//    public GradingResult gradeWithAI( String examId,  Map<String, String> answers) throws JsonProcessingException {
//        Exam exam=getExam(examId);
//        Map<String, String> quesList=exam.answers;
//        System.out.println(quesList);
//        return ExamCorrectingSevice.Correcting(quesList,answers);
//    }
//
//
//    // 教师对试卷进行二次批改或复核
//    //@PostMapping("/{examId}/review")
//    public GradingResult reviewExam( String examId,  Map<String, String> feedback) {
//        GradingResult result = getPreviousGradingResult(examId); // 获取先前的批改结果
//
//        // 教师修改批改结果
//        for (Map.Entry<String, String> entry : feedback.entrySet()) {
//            String questionId = entry.getKey();
//            String teacherFeedback = entry.getValue();
//
//            // 教师给反馈
//            result.addFeedback(questionId, teacherFeedback);
//            //result.addScore(questionId, 1);
//        }
//
//        return result;
//    }
//
//    // 获取之前的批改结果
//    private GradingResult getPreviousGradingResult(String examId) {
//
//        return new GradingResult(); // 返回一个批改结果
//    }
//}
