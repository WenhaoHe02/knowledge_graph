package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.QuesList;
import com.knowledgegraph.application.repository.ExerciseRepository;
import com.knowledgegraph.application.util.Neo4jUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ExamServiceImpl implements ExamService {

    private final ExerciseRepository exerciseRepository = new ExerciseRepository();
    private final Driver driver = Neo4jUtil.getDriver();

    @Override
    public JSONObject generateExam(JSONArray knowledgeIds) {
        Set<String> usedQuestionIds = new HashSet<>();
        JSONArray quesListArray = new JSONArray();

        for (int i = 0; i < knowledgeIds.length(); i++) {
            String knowledgeId = knowledgeIds.getString(i);
            List<QuesList> quesList = exerciseRepository.findExercisesByKnowledgePointId(knowledgeId);

            for (QuesList ques : quesList) {
                String questionId = ques.getTitleContent();
                if (!usedQuestionIds.contains(questionId)) {
                    usedQuestionIds.add(questionId);

                    JSONObject quesJson = new JSONObject();
                    quesJson.put("titleContent", ques.getTitleContent());
                    quesJson.put("standardAnswer", ques.getStandardAnswer());
                    quesListArray.put(quesJson);
                    break;
                }
            }
        }

        String examId = UUID.randomUUID().toString();
        String examTitle = "自动生成的试卷";

        writeExamToDatabase(examId, examTitle, quesListArray);

        JSONObject responseJson = new JSONObject();
        responseJson.put("code", "200");
        responseJson.put("examId", examId);
        responseJson.put("examTitle", examTitle);
        responseJson.put("quesList", quesListArray);
        responseJson.put("success", "true");

        return responseJson;
    }

    @Override
    public JSONObject saveExam(String examId) {
        JSONObject responseJson = new JSONObject();

        try (Session session = driver.session()) {
            String updateQuery = "MATCH (exam:试卷 {id: $examId}) " +
                    "SET exam.flag = true " +
                    "RETURN exam.id AS id, exam.title AS title, exam.quesList AS quesList";
            Result result = session.run(updateQuery, org.neo4j.driver.Values.parameters("examId", examId));

            if (result.hasNext()) {
                Record record = result.next();

                String savedExamId = record.get("id").asString();
                String savedExamTitle = record.get("title").asString();
                String savedQuesListString = record.get("quesList").asString();

                deleteUnsavedExams(session);

                responseJson.put("code", "200");
                responseJson.put("examId", savedExamId);
                responseJson.put("examTitle", savedExamTitle);
                responseJson.put("quesList", new JSONArray(savedQuesListString));
                responseJson.put("success", "true");
            } else {
                responseJson.put("code", "404");
                responseJson.put("success", "false");
                responseJson.put("message", "试卷未找到");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.put("code", "500");
            responseJson.put("success", "false");
            responseJson.put("message", "服务器内部错误");
        }

        return responseJson;
    }

    private void deleteUnsavedExams(Session session) {
        String deleteQuery = "MATCH (exam:试卷) WHERE exam.flag = false DETACH DELETE exam";
        session.run(deleteQuery);
        System.out.println("已删除未保存的试卷节点");
    }

    private void writeExamToDatabase(String examId, String examTitle, JSONArray quesListArray) {
        try (Session session = driver.session()) {
            String quesListString = quesListArray.toString();
            String query = "CREATE (exam:试卷 {id: $examId, title: $examTitle, quesList: $quesList, flag: $flag})";
            session.run(query, org.neo4j.driver.Values.parameters(
                    "examId", examId,
                    "examTitle", examTitle,
                    "quesList", quesListString,
                    "flag", false
            ));
            System.out.println("试卷已写入数据库: " + examId);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("试卷写入数据库失败: " + e.getMessage());
        }
    }
}
