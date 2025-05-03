package org.example;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.summary.ResultSummary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * 总线核心类（适配原DataBaseBus）
 */
public class Bus {
    private final EDataReader dataReader = new EDataReader();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void sendRequestAsync(DbRequest request) {
        executor.submit(() -> {
            DbResponse response = executeDbRequest(request);
            BusManager.getInstance().completeResponse(response); // 完成 future
        });
    }

    private DbResponse executeDbRequest(DbRequest dbRequest) {
        return switch (dbRequest.getOperation()) {
            case READ -> handleReadOperation(dbRequest);
            case CREATE, UPDATE, DELETE -> handleWriteOperation(dbRequest);
        };
    }

    private DbResponse handleReadOperation(DbRequest request) {
        try {
            List<KnowledgePoint> records = dataReader.executeQuery(request.getCypher(), request.getParameters(), mapToKnowledgePoint());
            return new DbResponse(
                    request.getCorrelationId(),
                    true,
                    request.getOperation(),
                    records
            );
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            return new DbResponse(
                    request.getCorrelationId(),
                    false,
                    request.getOperation(),
                    null
            );
        }
    }


    private DbResponse handleWriteOperation(DbRequest request) {
        List<KnowledgePoint> records = dataReader.executeQuery(request.getCypher(), null, mapToKnowledgePoint());
        return new DbResponse(
                request.getCorrelationId(),
                true,
                request.getOperation(),
                records
        );
    }

    private Function<Record, KnowledgePoint> mapToKnowledgePoint() {
        // 你原来的字段提取代码保持不变
        return record -> {
            String id = record.get("kp").get("id").asString();
            String name = record.get("kp").get("name").asString();
            String content = record.get("kp").get("content").asString();
            return new KnowledgePoint(id, name, content);
        };
    }
}
