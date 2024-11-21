package com.knowledgegraph.application.repository;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.knowledgegraph.application.model.Exam;
import com.knowledgegraph.application.model.GradingResult;
import com.knowledgegraph.application.model.KnowledgePoint;
import com.knowledgegraph.application.util.DataReader;
import com.knowledgegraph.application.util.Neo4jUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import scala.Int;

import java.util.*;
import java.util.function.Function;

class Ques{
    public void setQues(String ques) {
        this.titleContent = ques;
    }

    public void setAns(String ans) {
        this.standardAnswer = ans;
    }

    public String getQues() {
        return titleContent;
    }

    public String getAns() {
        return standardAnswer;
    }

    private String titleContent;
    private String standardAnswer;
}

public class ExamCorrectingReposity {
    // 使用Neo4jUtil获取Driver
    private final DataReader dataReader = new DataReader();

    public void addCorrectingRes(String userAnsId, GradingResult gradRes)
    {
        String matchQuery="MATCH (n:`用户作答` {id: \""+userAnsId+"\"})\n";

        Vector<String> v=new Vector();
        Vector<Integer> u=new Vector();
        for (String name : gradRes.getFeedback().keySet()) {
            Integer score=gradRes.getScores().get(name);
            u.add(score);
            String feedback=gradRes.getFeedback().get(name);
            v.add("\""+feedback+"\"");
        }

        String createQuery="CREATE (p:批改结果 {scores:"+u+",feedbacks:"+v+",totalScore:"+gradRes.getTotalScore()+"})-[:correctRes]->(n)";
        System.out.println(matchQuery+createQuery);
        dataReader.executeQuery(matchQuery+createQuery, mapToExam());
        //System.out.println(matchQuery+createQuery);
    }

    public void addUserAns(String userAnsId,String examId,Map<String, String> answers)
    {
        String matchQuery = "MATCH (n:试卷 {id:"+examId+"})";
        Vector<String> v=new Vector();
        for (String name : answers.keySet()) {
            String answer=answers.get(name);
            v.add("\""+answer+"\"");
        }
        String createQuery="CREATE (p:用户作答 {answers:"+v+",id:\""+userAnsId+"\"})-[:answer_relation]->(n)";
        System.out.println(matchQuery+createQuery);
        dataReader.executeQuery(matchQuery+createQuery, mapToExam());
    }

    public Exam searchExam(String examId) {
        String query = "MATCH (n:试卷 {id:"+examId+"}) return n";
        List<Exam> tmp= dataReader.executeQuery(query, mapToExam());
        return tmp.get(0);
    }

    public Exam searchExam2(String examId)
    {
        String query = "MATCH (n:试卷 {id:\""+examId+"\"}) return n";
        System.out.println("???///");
        List<Exam> tmp= dataReader.executeQuery(query, mapToExam2());
        return tmp.get(0);
    }


    public Map<String, String> searchQues(List<Integer> num){
        Map<String, String> res=new HashMap<>();
        for (Integer n : num) {
            String query = "MATCH (n:习题 {id:"+n+"}) return n";
            List<Ques> tmp=dataReader.executeQuery(query, mapToQues());
            res.put(tmp.get(0).getQues(),tmp.get(0).getAns());
        }
        return res;
    }


    private Function<Record, Exam> mapToExam() {
        return record -> {
            Exam ex = new Exam();
            //System.out.println(record.values().get(0).get("id"));
            ex.setExamId(record.values().get(0).get("id").toString());
            ex.setExamName(record.values().get(0).get("examName").asString());
            Map<String, String> answers;
            List<Integer> num = record.values().get(0).get("quesList").asList(Value::asInt);
            answers=searchQues(num);
            ex.setAnswers(answers);
            return ex;
        };
    }

    private Function<Record, Ques> mapToQues() {
        return record -> {
            Ques qs = new Ques();
            //System.out.println(record.values().get(0).get("question"));
            qs.setQues(record.values().get(0).get("question").asString());
            qs.setAns(record.values().get(0).get("answer").asString());
            return qs;
        };
    }

    private Function<Record, Exam> mapToExam2() {
        return record -> {
            Exam ex = new Exam();
            //System.out.println(record.values().get(0).get("id"));
            ex.setExamId(record.values().get(0).get("id").toString());
            ex.setExamName(record.values().get(0).get("title").asString());
            Map<String, String> answers = new HashMap<>();
            String tmp=record.values().get(0).get("quesList").asString();
            Gson gson = new Gson();
            // 解析 JSON 为 List<Ques> 对象
            List<Ques> qs = gson.fromJson(tmp, new TypeToken<List<Ques>>(){}.getType());
            System.out.println(qs);
            for(Ques q:qs)
                answers.put(q.getQues(),q.getAns());
            ex.setAnswers(answers);
            ex.print();
            return ex;
        };
    }
}
