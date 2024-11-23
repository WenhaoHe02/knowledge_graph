package com.knowledgegraph.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgegraph.application.model.DetailKnowledgePoint;
import com.knowledgegraph.application.repository.ModifyRepository;

public class ModifyServiceImpl implements ModifyService{
    private ModifyRepository modifyRepository = new ModifyRepository();
    @Override
    public String modifyKnowledge(String requestBody) throws JsonProcessingException{
        DetailKnowledgePoint params = transformToPoint(requestBody);
        return  modifyRepository.modifyKnowledgePoint(params);
    }
    private DetailKnowledgePoint transformToPoint(String requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        DetailKnowledgePoint detailKnowledgePoint = mapper.readValue(requestBody, DetailKnowledgePoint.class);
        return detailKnowledgePoint;
    }
}
