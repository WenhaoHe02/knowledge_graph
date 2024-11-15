package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.DetailKnowledgePoint;

import java.util.ArrayList;
import java.util.List;

public interface DetailService {
    DetailKnowledgePoint searchDetailKnowledgePoints(String id);
}
