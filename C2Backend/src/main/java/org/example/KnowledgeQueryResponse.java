package org.example;

import java.util.List;


/**
 * 知识点查询的响应，继承自 Response，result 是 List<KnowledgePoint>。
 */
public class KnowledgeQueryResponse extends Response {

    public KnowledgeQueryResponse(
            String correlationId,
            boolean success,
            List<KnowledgePoint> result,
            String sourceComponent) {

        super("knowledge_query_response", correlationId, success, result, sourceComponent);
    }

    @SuppressWarnings("unchecked")
    public List<KnowledgePoint> getKnowledgePoints() {
        return (List<KnowledgePoint>) getResult();
    }
}


