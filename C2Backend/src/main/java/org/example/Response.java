package org.example;

import java.util.Objects;

/**
 * Response 是 C2 架构中所有响应的基础类。
 * 可用于组件返回、数据库查询结果等场景。
 */
public class Response {

    private final String eventType;
    private final String correlationId;
    private final boolean success;
    private final Object result;
    private final String sourceComponent;

    public Response(String eventType, String correlationId, boolean success, Object result, String sourceComponent) {
        this.eventType = eventType;
        this.correlationId = correlationId;
        this.success = success;
        this.result = result;
        this.sourceComponent = sourceComponent;
    }

    public String getEventType() {
        return eventType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getResult() {
        return result;
    }

    public String getSourceComponent() {
        return sourceComponent;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "eventType='" + eventType + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", success=" + success +
                ", result=" + result +
                ", sourceComponent='" + sourceComponent + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response)) return false;
        Response that = (Response) o;
        return success == that.success &&
                Objects.equals(eventType, that.eventType) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(result, that.result) &&
                Objects.equals(sourceComponent, that.sourceComponent);
    }
}
