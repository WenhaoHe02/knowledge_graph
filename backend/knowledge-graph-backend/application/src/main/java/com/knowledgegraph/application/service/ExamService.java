package com.knowledgegraph.application.service;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ExamService {
    JSONObject generateExam(JSONArray knowledgeIds);
}
