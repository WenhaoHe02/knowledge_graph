package org.example;

import java.util.Objects;

/**
 * DbResponse 是针对 Neo4j 操作的通用响应封装。
 * 可支持查询（READ）、新增（CREATE）、更新（UPDATE）、删除（DELETE）等。
 */
public class DbResponse {

    private final String correlationId;
    private final boolean success;
    private final OperationType operation;
    private final Object result;

    public DbResponse(String correlationId, boolean success, OperationType operation, Object result) {
        this.correlationId = correlationId;
        this.success = success;
        this.operation = operation;
        this.result = result;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public boolean isSuccess() {
        return success;
    }

    public OperationType getOperation() {
        return operation;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "DbResponse{" +
                "correlationId='" + correlationId + '\'' +
                ", success=" + success +
                ", operation=" + operation +
                ", result=" + result +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbResponse)) return false;
        DbResponse that = (DbResponse) o;
        return success == that.success &&
                Objects.equals(correlationId, that.correlationId) &&
                operation == that.operation &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, success, operation, result);
    }
}
