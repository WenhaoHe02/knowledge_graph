package org.example;
public class Request {
    protected final String eventType;
    protected final Object payload;
    protected final String correlationId;
    protected final String sourceComponent; // optional
    protected final String targetComponent; // optional

    public Request(String eventType, Object payload, String correlationId,
                   String sourceComponent, String targetComponent) {
        this.eventType = eventType;
        this.payload = payload;
        this.correlationId = correlationId;
        this.sourceComponent = sourceComponent;
        this.targetComponent = targetComponent;
    }

    // Getter 略...

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "eventType='" + eventType + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", sourceComponent='" + sourceComponent + '\'' +
                ", targetComponent='" + targetComponent + '\'' +
                ", payload=" + payload +
                '}';
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
