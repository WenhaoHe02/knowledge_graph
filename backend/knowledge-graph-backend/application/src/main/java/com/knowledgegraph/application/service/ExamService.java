package com.knowledgegraph.application.service;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ExamService {
    /**
     * 生成试卷
     *
     * @param knowledgeIds 知识点 ID 列表
     * @return 生成的试卷信息
     */
    JSONObject generateExam(JSONArray knowledgeIds);

    /**
     * 保存试卷
     *
     * @param examId 试卷 ID
     * @return 保存结果
     */
    JSONObject saveExam(String examId);

    /**
     * 根据试卷 ID 获取试卷内容
     *
     * @param examId 试卷 ID，如果为空返回全部试卷
     * @return 试卷内容
     */
    JSONArray getExam(String examId);
}
