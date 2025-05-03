package org.example;

import java.util.Map;
import java.util.UUID;

/**
 * 查询知识点的业务事件，继承自 Request。
 * payload 通常包含 { "keyword": "xxx" }。
 */
public class KnowledgeQueryRequest extends Request {

    public KnowledgeQueryRequest(
            Map<String, String> payload,
            String sourceComponent,
            String targetComponent, String correlationId) {

        super("knowledge_query_request", payload, correlationId, sourceComponent, targetComponent);
    }

    @SuppressWarnings("unchecked")
    public String getKeyword() {
        Object raw = payload instanceof Map ? ((Map<?, ?>) payload).get("keyword") : null;
        return raw != null ? raw.toString() : "";
    }
}
