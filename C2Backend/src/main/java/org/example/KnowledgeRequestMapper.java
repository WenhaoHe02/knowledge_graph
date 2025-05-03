package org.example;

import java.util.Map;

public class KnowledgeRequestMapper {

    public static DbRequest toDbRequest(KnowledgeQueryRequest request) {
        String cypher = "MATCH (kp:知识点) WHERE kp.name CONTAINS $keyword RETURN kp";
        Map<String, Object> parameters = Map.of("keyword", request.getKeyword());

        return new DbRequest(
                request.getCorrelationId(),
                OperationType.READ,
                cypher,
                parameters
        );
    }
}
