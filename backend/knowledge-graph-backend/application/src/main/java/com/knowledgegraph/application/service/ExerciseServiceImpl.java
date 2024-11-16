package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.QuesList;
import com.knowledgegraph.application.repository.ExerciseRepository;

import java.util.List;

public class ExerciseServiceImpl implements ExerciseService {
    private ExerciseRepository exerciseRepository = new ExerciseRepository();

    @Override
    public List<QuesList> getExercisesByKnowledgePointId(String id) {
        return exerciseRepository.findExercisesByKnowledgePointId(id);
    }
}
