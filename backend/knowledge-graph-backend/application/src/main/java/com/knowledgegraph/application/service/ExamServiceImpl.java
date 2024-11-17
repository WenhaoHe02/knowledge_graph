package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.QuesList;
import com.knowledgegraph.application.repository.ExerciseRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ExamServiceImpl implements ExamService {
    private ExerciseRepository exerciseRepository = new ExerciseRepository();

    @Override
    public JSONObject generateExam(JSONArray knowledgeIds) {
        // 创建一个集合用于记录已经选过的题目 ID，避免重复
        Set<String> usedQuestionIds = new HashSet<>();
        JSONArray quesListArray = new JSONArray();

        for (int i = 0; i < knowledgeIds.length(); i++) {
            String knowledgeId = knowledgeIds.getString(i);

            // 查询该知识点的相关习题
            List<QuesList> quesList = exerciseRepository.findExercisesByKnowledgePointId(knowledgeId);

            // 挑选一个未使用的题目
            for (QuesList ques : quesList) {
                String questionId = ques.getTitleContent(); // 假设题目内容唯一标识题目
                if (!usedQuestionIds.contains(questionId)) {
                    usedQuestionIds.add(questionId);

                    JSONObject quesJson = new JSONObject();
                    quesJson.put("titleContent", ques.getTitleContent());
                    quesJson.put("standardAnswer", ques.getStandardAnswer());

                    quesListArray.put(quesJson);
                    break; // 每个知识点只选一个题目
                }
            }
        }

        // 构建返回的试卷信息
        JSONObject responseJson = new JSONObject();
        responseJson.put("code", "200");
        responseJson.put("examId", UUID.randomUUID().toString()); // 生成唯一试卷 ID
        responseJson.put("examTitle", "自动生成的试卷");
        responseJson.put("quesList", quesListArray);
        responseJson.put("success", "true");

        return responseJson;
    }
}
