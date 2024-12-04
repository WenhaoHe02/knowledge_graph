package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.QuesList;
import com.knowledgegraph.application.repository.ExerciseRepository;

import java.util.List;

public class ExerciseServiceImpl implements ExerciseService {
    private ExerciseRepository exerciseRepository = new ExerciseRepository();

    @Override
    public List<QuesList> getExercisesByKnowledgePointId(String id) {
        // 调用无数量限制的版本
        return exerciseRepository.findExercisesByKnowledgePointId(id);
    }

    public List<QuesList> getExercisesByKnowledgePointId(String id, int limit) {
        // 调用带数量限制的版本
        return exerciseRepository.findExercisesByKnowledgePointId(id, limit);
    }
}
