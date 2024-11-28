package com.knowledgegraph.application.service;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ExamService {
    /**
     * 生成试卷
     *
     * @param knowledgeIds 知识点 ID 列表
     * @param username 用户名
     * @return 生成的试卷信息
     */
    JSONObject generateExam(JSONArray knowledgeIds, String username);

    /**
     * 保存试卷
     *
     * @param examId 试卷 ID
     * @return 保存结果
     */
    JSONObject saveExam(String examId);

    /**
     * 根据试卷 ID 和用户名获取试卷内容
     *
     * @param examId 试卷 ID，如果为空返回全部试卷
     * @param username 用户名
     * @return 试卷内容
     */
    JSONArray getExam(String examId, String username);

    /**
     * 提交试卷
     *
     * @param examId 试卷的唯一标识符
     * @param answers 用户提交的答案列表
     * @param username 用户名
     * @return 提交是否成功
     */
    boolean submitExam(String examId, JSONArray answers, String username);

    /**
     * 删除试卷
     *
     * @param examId 试卷的唯一标识符
     * @return 是否成功删除
     */
    boolean deleteExam(String examId);

    /**
     * 根据试卷 ID 和用户名获取用户作答
     *
     * @param examId 试卷的唯一标识符
     * @param username 用户名
     * @return 用户作答列表
     */
    JSONArray getAnswersByExamId(String examId, String username);
}