package com.knowledgegraph.application.service;

import com.knowledgegraph.application.model.QuesList;
import java.util.List;

public interface ExerciseService {
    List<QuesList> getExercisesByKnowledgePointId(String id);
}
