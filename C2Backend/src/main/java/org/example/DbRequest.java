package org.example;

import java.util.Map;
import java.util.Objects;

/**
 * DbRequest 表示对 Neo4j 发起的一次数据库请求。
 * 支持增删查改操作，通过 operationType 明确操作类型。
 */
public class DbRequest {
    private final String correlationId;
    private final OperationType operation;
    private final String cypher;
    private final Map<String, Object> parameters;

    public DbRequest(String correlationId, OperationType operation, String cypher, Map<String, Object> parameters) {
        this.correlationId = correlationId;
        this.operation = operation;
        this.cypher = cypher;
        this.parameters = parameters;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public OperationType getOperation() {
        return operation;
    }

    public String getCypher() {
        return cypher;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "DbRequest{" +
                "correlationId='" + correlationId + '\'' +
                ", operation=" + operation +
                ", cypher='" + cypher + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbRequest)) return false;
        DbRequest that = (DbRequest) o;
        return Objects.equals(correlationId, that.correlationId) &&
                operation == that.operation &&
                Objects.equals(cypher, that.cypher) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, operation, cypher, parameters);
    }
}
