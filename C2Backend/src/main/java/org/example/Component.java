package org.example;


public interface Component {
    String getComponentId();

    ApiRequest receiveFromFront();

    void sendToManager(DbRequest dbRequest);

    DbResponse receiveFromManager(String correlationId);

    boolean sendToFront(ApiResponse response);

    void process();
}

