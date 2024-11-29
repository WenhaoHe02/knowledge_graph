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
            // 查询习题的 ID、内容、标准答案和类型
            String query = "MATCH (q:习题)-[:exercise_relation]->(kp:知识点 {id: $id}) " +
                    "RETURN q.id AS exerciseId, q.question AS titleContent, q.answer AS standardAnswer, q.type AS type";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("id", id));

            // 遍历查询结果，构造 QuesList 对象并添加到列表
            while (result.hasNext()) {
                Record record = result.next();

                // 获取 ID 并显式处理 INTEGER 类型的 exerciseId
                String exerciseId = record.get("exerciseId").type().name().equals("INTEGER") ?
                        String.valueOf(record.get("exerciseId").asInt()) :
                        record.get("exerciseId").asString();

                String titleContent = record.get("titleContent").asString();
                String standardAnswer = record.get("standardAnswer").asString();

                // 检查 type 是否为 NULL
                int type = record.get("type").isNull() ? 1 : record.get("type").asInt();

                QuesList ques = new QuesList(exerciseId, titleContent, standardAnswer, type);
                quesLists.add(ques);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quesLists;
    }

    /**
     * 根据知识点 ID 查询知识点名称
     *
     * @param knowledgeId 知识点 ID
     * @return 知识点名称
     */
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

    /**
     * 根据知识点 ID 查询相关的所有题目 ID
     *
     * @param id 知识点 ID
     * @return 题目 ID 列表
     */
    public List<String> findExercisesIdByKnowledgePointId(String id) {
        List<String> exerciseIds = new ArrayList<>();

        try (Session session = driver.session()) {
            // 查询所有指向该知识点的习题 ID
            String query = "MATCH (q:习题)-[:exercise_relation]->(kp:知识点 {id: $id}) " +
                    "RETURN q.id AS exerciseId";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("id", id));

            // 遍历查询结果，添加到列表
            while (result.hasNext()) {
                Record record = result.next();

                // 显式处理 INTEGER 类型的 exerciseId
                if (record.get("exerciseId").type().name().equals("INTEGER")) {
                    exerciseIds.add(String.valueOf(record.get("exerciseId").asInt()));
                } else {
                    exerciseIds.add(record.get("exerciseId").asString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exerciseIds;
    }
}