package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

class ApiRequest {
    private final String correlationId;
    private final Object payload;

    public ApiRequest(String correlationId, Object payload) {
        this.correlationId = correlationId;
        this.payload = payload;
    }

    public String getCorrelationId() { return correlationId; }
    public Object getPayload()       { return payload; }
}

class ApiResponse {
    private final String correlationId;
    private final boolean success;
    private final Object result;

    public ApiResponse(String correlationId, boolean success, Object result) {
        this.correlationId = correlationId;
        this.success = success;
        this.result = result;
    }

    public String getCorrelationId() { return correlationId; }
    public boolean isSuccess()       { return success; }
    public Object getResult()        { return result; }
}

public class KnowledgeQueryComponent implements Component {

    private final String componentId = "knowledge_query_component";
    private final BlockingQueue<ApiRequest> queue = new LinkedBlockingQueue<>();
    private final Map<String, HttpExchange> pendingHttp = new ConcurrentHashMap<>();

    public KnowledgeQueryComponent() {
        startHttpServer();
        new Thread(this::process).start();
    }

    @Override
    public String getComponentId() {
        return componentId;
    }

    private void startHttpServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);
            server.createContext("/query", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                        exchange.sendResponseHeaders(405, -1);
                        return;
                    }

                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    String cid = UUID.randomUUID().toString();
                    ApiRequest req = new ApiRequest(cid, body);
                    pendingHttp.put(cid, exchange);
                    queue.offer(req);
                    System.out.println("[Front] 请求已接收: " + cid);
                }
            });
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("[HTTP] 接口已启动: http://localhost:9000/query");
        } catch (IOException e) {
            throw new RuntimeException("启动 HTTP 服务失败", e);
        }
    }

    @Override
    public ApiRequest receiveFromFront() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("接收前端请求失败", e);
        }
    }

    @Override
    public void sendToManager(DbRequest dbRequest) {
        System.out.println("[" + componentId + "] 发请求到 Manager: " + dbRequest.getCorrelationId());
        BusManager.getInstance().receive(dbRequest,  dbRequest.getCorrelationId());
    }

    @Override
    public DbResponse receiveFromManager(String correlationId) {
        return BusManager.getInstance().waitForResponse(correlationId);
    }

    @Override
    public boolean sendToFront(ApiResponse response) {
        HttpExchange exchange = pendingHttp.remove(response.getCorrelationId());
        if (exchange == null) {
            System.err.println("未找到对应请求上下文，响应失败");
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(response);

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
            System.out.println("[" + componentId + "] JSON 响应已发送");
            return true;
        } catch (IOException e) {
            System.err.println("发送响应失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void process() {
        while (true) {
            // Step 1: 接收前端请求
            ApiRequest apiReq = receiveFromFront();
            String keyword = apiReq.getPayload() != null ? apiReq.getPayload().toString().trim() : "";
            System.out.println("[Mapper] keyword = '" + keyword + "'");

            // Step 2: 构造组件层业务请求
            KnowledgeQueryRequest queryReq = new KnowledgeQueryRequest(
                    Map.of("keyword", keyword),
                    getComponentId(),
                    "db_component",
                    apiReq.getCorrelationId()
            );

            // Step 3: 将业务请求映射为数据库请求
            DbRequest dbReq = KnowledgeRequestMapper.toDbRequest(queryReq);

            // Step 4: 发请求到 Manager
            sendToManager(dbReq);

            // Step 5: 收数据库响应
            DbResponse dbResp = receiveFromManager(dbReq.getCorrelationId());

            // Step 6: 映射为业务响应
            KnowledgeQueryResponse bizResp = KnowledgeResponseMapper.fromDbResponse(dbResp);

            // Step 7: 返回给前端
            sendToFront(new ApiResponse(
                    bizResp.getCorrelationId(),
                    bizResp.isSuccess(),
                    bizResp.getKnowledgePoints()
            ));
        }
    }

    public void test()
    {
       process();
    }
}