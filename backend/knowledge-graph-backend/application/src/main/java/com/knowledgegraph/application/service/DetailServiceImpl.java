package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.DetailKnowledgePoint;
import com.knowledgegraph.application.repository.DetailRepository;
import java.util.List;

public class DetailServiceImpl implements DetailService{
    private DetailRepository detailRepository = new DetailRepository();
    @Override
    public DetailKnowledgePoint searchDetailKnowledgePoints(String id) {
        return detailRepository.searchDetailKnowledgePoints(id);
    }
}
