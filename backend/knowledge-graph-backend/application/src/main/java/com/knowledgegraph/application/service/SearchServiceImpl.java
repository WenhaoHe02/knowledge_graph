package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.KnowledgePoint;
import com.knowledgegraph.application.repository.SearchRepository;
import java.util.List;

public class SearchServiceImpl implements SearchService {

    private SearchRepository searchRepository = new SearchRepository();

    @Override
    public List<KnowledgePoint> searchKnowledgePoints(String keyword) {
        return searchRepository.searchKnowledgePoints(keyword);
    }
}
