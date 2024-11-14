package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.Exam;
import com.knowledgegraph.application.model.KnowledgePoint;
import com.knowledgegraph.application.util.Neo4jUtil;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;

public class ExamCorrectingReposity {
    // 使用Neo4jUtil获取Driver
    private final Driver driver = Neo4jUtil.getDriver();

    public Exam searchExam(String examId) {
        Exam exam = new Exam();
        try (Session session = driver.session()) {
            Result result = session.run(
                    "MATCH (ex:Exam) WHERE ex.examId CONTAINS $examId RETURN ex",
                    org.neo4j.driver.Values.parameters("examId", examId)
            );
            while (result.hasNext()) {
                Record record = result.next();
                KnowledgePoint kp = new KnowledgePoint();
                exam.setExamName(record.get("ex").get("examName").asString());
                exam.setExamId(record.get("ex").get("examId").asString());
                //exam.setAnswers(record.get("ex").get("answers").asString());
            }
        }
        return exam;
    }
}
