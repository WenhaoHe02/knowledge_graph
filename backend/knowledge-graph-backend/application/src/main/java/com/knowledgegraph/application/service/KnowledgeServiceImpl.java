package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.KnowledgePoint;
import com.knowledgegraph.application.repository.KnowledgeRepository;
import java.util.List;

public class KnowledgeServiceImpl implements KnowledgeService {

    private KnowledgeRepository knowledgeRepository = new KnowledgeRepository();

    @Override
    public List<KnowledgePoint> searchKnowledgePoints(String keyword) {
        return knowledgeRepository.searchKnowledgePoints(keyword);
    }
}
