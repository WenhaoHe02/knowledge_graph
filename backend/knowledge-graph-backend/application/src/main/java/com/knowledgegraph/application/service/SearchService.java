package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.KnowledgePoint;
import java.util.List;

public interface SearchService {
    List<KnowledgePoint> searchKnowledgePoints(String keyword);
}