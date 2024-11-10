package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.KnowledgePoint;
import java.util.List;

public interface KnowledgeService {
    List<KnowledgePoint> searchKnowledgePoints(String keyword);
}