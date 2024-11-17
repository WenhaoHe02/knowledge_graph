package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.Exam;
import com.knowledgegraph.application.model.KnowledgePoint;
import com.knowledgegraph.application.util.DataReader;
import com.knowledgegraph.application.util.Neo4jUtil;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class Ques{
    public void setQues(String ques) {
        this.ques = ques;
    }

    public void setAns(String ans) {
        this.ans = ans;
    }

    public String getQues() {
        return ques;
    }

    public String getAns() {
        return ans;
    }

    private String ques;
    private String ans;
}

public class ExamCorrectingReposity {
    // 使用Neo4jUtil获取Driver
    private final DataReader dataReader = new DataReader();

    public Exam searchExam(String examId) {
        String query = "MATCH (n:试卷 {id:"+examId+"}) return n";
        List<Exam> tmp= dataReader.executeQuery(query, mapToExam());
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
}
