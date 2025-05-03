package org.example;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeResponseMapper {

    @SuppressWarnings("unchecked")
    public static KnowledgeQueryResponse fromDbResponse(DbResponse dbResponse) {

        List<KnowledgePoint> points;

        // 👉 dbResponse.result 已经是 List<KnowledgePoint>
        if (dbResponse.getResult() instanceof List<?> list
                && !list.isEmpty()
                && list.get(0) instanceof KnowledgePoint) {
            points = (List<KnowledgePoint>) list;
        } else {
            // 类型不符就给空列表并打印一下，方便排错
            System.err.println("[Mapper] result 不是 KnowledgePoint 列表: " + dbResponse.getResult());
            points = new ArrayList<>();
        }

        return new KnowledgeQueryResponse(
                dbResponse.getCorrelationId(),
                dbResponse.isSuccess(),
                points,
                "knowledge_query_component"
        );
    }
}
