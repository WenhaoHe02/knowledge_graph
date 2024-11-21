package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.QuesList;
import com.knowledgegraph.application.util.Neo4jUtil;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

public class ExerciseRepository {

    private final Driver driver = Neo4jUtil.getDriver();

    /**
     * 根据知识点 ID 查询相关的所有题目
     *
     * @param id 知识点 ID
     * @return 题目列表
     */
    public List<QuesList> findExercisesByKnowledgePointId(String id) {
        List<QuesList> quesLists = new ArrayList<>();

        try (Session session = driver.session()) {
            // 查询所有指向该知识点的习题
            String query = "MATCH (q:习题)-[:exercise_relation]->(kp:知识点 {id: $id}) " +
                    "RETURN q.question AS titleContent, q.answer AS standardAnswer";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("id", id));

            // 遍历查询结果，添加到列表
            while (result.hasNext()) {
                Record record = result.next();
                String titleContent = record.get("titleContent").asString();
                String standardAnswer = record.get("standardAnswer").asString();

                QuesList ques = new QuesList(titleContent, standardAnswer);
                quesLists.add(ques);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quesLists;
    }

    public String findKnowledgePointNameById(String knowledgeId) {
        String knowledgeName = null;

        try (Session session = Neo4jUtil.getDriver().session()) {
            String query = "MATCH (kp:知识点 {id: $id}) RETURN kp.name AS name";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("id", knowledgeId));

            if (result.hasNext()) {
                Record record = result.next();
                knowledgeName = record.get("name").asString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return knowledgeName;
    }
}
