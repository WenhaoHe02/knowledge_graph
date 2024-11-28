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
        JSONArray quesIdsArray = new JSONArray(); // 新增 quesIds 数组

        // 拼接知识点名称部分
        StringBuilder titleBuilder = new StringBuilder();
        for (int i = 0; i < knowledgeIds.length(); i++) {
            String knowledgeId = knowledgeIds.getString(i);

            // 查询知识点名称
            String knowledgeName = exerciseRepository.findKnowledgePointNameById(knowledgeId);

            // 将知识点名称添加到标题
            titleBuilder.append(knowledgeName != null ? knowledgeName : knowledgeId); // 如果名称为空则使用 ID
            if (i < knowledgeIds.length() - 1) {
                titleBuilder.append(", "); // 逗号分隔
            }

            // 查询相关习题
            List<QuesList> quesList = exerciseRepository.findExercisesByKnowledgePointId(knowledgeId);

            for (QuesList ques : quesList) {
                String questionId = ques.getId(); // 使用习题 ID
                if (!usedQuestionIds.contains(questionId)) {
                    usedQuestionIds.add(questionId);

                    // 添加到 quesListArray
                    JSONObject quesJson = new JSONObject();
                    quesJson.put("titleContent", ques.getTitleContent());
                    quesJson.put("standardAnswer", ques.getStandardAnswer());
                    quesListArray.put(quesJson);

                    // 添加到 quesIdsArray
                    quesIdsArray.put(questionId);
                }
            }
        }

        // 获取当前时间并格式化为 "yyyy-MM-dd HH:mm:ss"
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 构建试卷标题
        String examTitle = titleBuilder.append(" - ").append(timestamp).toString();

        String examId = UUID.randomUUID().toString();
        writeExamToDatabase(examId, examTitle, quesListArray, quesIdsArray);

        JSONObject responseJson = new JSONObject();
        responseJson.put("code", "200");
        responseJson.put("examId", examId);
        responseJson.put("examTitle", examTitle);
        responseJson.put("quesList", quesListArray);
        responseJson.put("quesIds", quesIdsArray); // 将 quesIds 返回
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

    private void writeExamToDatabase(String examId, String examTitle, JSONArray quesListArray, JSONArray quesIdsArray) {
        try (Session session = driver.session()) {
            String query = "CREATE (exam:试卷 {id: $examId, title: $examTitle, quesList: $quesList, quesIds: $quesIds, flag: $flag})";
            session.run(query, org.neo4j.driver.Values.parameters(
                    "examId", examId,
                    "examTitle", examTitle,
                    "quesList", quesListArray.toString(), // 存储为字符串
                    "quesIds", quesIdsArray.toString(), // 存储为字符串
                    "flag", false
            ));
            System.out.println("试卷已写入数据库: " + examId);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("试卷写入数据库失败: " + e.getMessage());
        }
    }

    @Override
    public JSONArray getExam(String examId) {
        JSONArray examsArray = new JSONArray();

        try (Session session = driver.session()) {
            String query;

            // 根据是否传入 examId 构建查询
            if (examId != null && !examId.isEmpty()) {
                query = "MATCH (exam:试卷 {id: $examId}) RETURN exam.id AS id, exam.title AS title, exam.quesList AS quesList, exam.quesIds AS quesIds";
                Result result = session.run(query, org.neo4j.driver.Values.parameters("examId", examId));

                if (result.hasNext()) {
                    Record record = result.next();
                    examsArray.put(buildExamJson(record));
                }
            } else {
                query = "MATCH (exam:试卷) RETURN exam.id AS id, exam.title AS title, exam.quesList AS quesList, exam.quesIds AS quesIds";
                Result result = session.run(query);

                while (result.hasNext()) {
                    Record record = result.next();
                    examsArray.put(buildExamJson(record));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return examsArray;
    }

    /**
     * 构建试卷的 JSON 格式
     *
     * @param record 数据库查询结果记录
     * @return 试卷 JSON 对象
     */
    private JSONObject buildExamJson(Record record) {
        JSONObject examJson = new JSONObject();

        // 获取试卷 ID 和标题
        String examId = record.get("id").type().name().equals("INTEGER") ?
                String.valueOf(record.get("id").asInt()) :
                record.get("id").asString();

        String examTitle = record.get("title").type().name().equals("INTEGER") ?
                String.valueOf(record.get("title").asInt()) :
                record.get("title").asString();

        // 获取 quesList
        JSONArray quesListArray = new JSONArray();
        try {
            quesListArray = new JSONArray(record.get("quesList").asString());
        } catch (Exception e) {
            // 如果解析失败，返回空数组
            quesListArray = new JSONArray();
        }

        // 获取 quesIds
        JSONArray quesIdsArray = new JSONArray();
        try {
            quesIdsArray = new JSONArray(record.get("quesIds").asString());
        } catch (Exception e) {
            // 如果解析失败，返回空数组
            quesIdsArray = new JSONArray();
        }

        // 构建响应 JSON
        examJson.put("code", "200");
        examJson.put("examId", examId);
        examJson.put("examTitle", examTitle);
        examJson.put("quesIds", quesIdsArray);
        examJson.put("quesList", quesListArray); // 包含题目详细信息
        examJson.put("success", "true");

        return examJson;
    }


    @Override
    public boolean submitExam(String examId, JSONArray answers) {
        try (Session session = driver.session()) {
            // 创建用户作答节点
            String userAnswerId = UUID.randomUUID().toString();
            String createUserAnswerQuery = "CREATE (ua:用户作答 {id: $userAnswerId, answers: $answers})";
            session.run(createUserAnswerQuery, org.neo4j.driver.Values.parameters(
                    "userAnswerId", userAnswerId,
                    "answers", answers.toString() // 转换为 JSON 字符串存储
            ));

            // 创建用户作答节点与试卷节点的关系
            String createRelationQuery = "MATCH (ua:用户作答 {id: $userAnswerId}), (exam:试卷 {id: $examId}) " +
                    "CREATE (ua)-[:answer_relation]->(exam)";
            session.run(createRelationQuery, org.neo4j.driver.Values.parameters(
                    "userAnswerId", userAnswerId,
                    "examId", examId
            ));

            System.out.println("用户作答已提交: 用户作答节点 ID = " + userAnswerId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean deleteExam(String examId) {
        try (Session session = driver.session()) {
            // 删除试卷节点
            String query = "MATCH (exam:试卷 {id: $examId}) DETACH DELETE exam RETURN COUNT(exam) AS count";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("examId", examId));

            if (result.hasNext()) {
                int count = result.next().get("count").asInt();
                return count > 0; // 如果删除了至少一个节点，返回 true
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // 如果发生异常或未删除任何节点，返回 false
    }

    @Override
    public JSONArray getAnswersByExamId(String examId) {
        JSONArray userAnswersArray = new JSONArray();

        try (Session session = driver.session()) {
            // Cypher 查询：获取用户作答节点及其答案
            String query = "MATCH (ua:用户作答)-[:answer_relation]->(exam:试卷 {id: $examId}) " +
                    "RETURN ua.id AS userAnswerId, ua.answers AS answers";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("examId", examId));

            // 遍历查询结果，构建用户作答 JSON
            while (result.hasNext()) {
                Record record = result.next();
                JSONObject userAnswerJson = new JSONObject();
                userAnswerJson.put("id", record.get("userAnswerId").asString());
                userAnswerJson.put("answers", new JSONArray(record.get("answers").asString()));
                userAnswersArray.put(userAnswerJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userAnswersArray;
    }
}
