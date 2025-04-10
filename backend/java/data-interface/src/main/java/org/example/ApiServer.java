package org.example;

// src/main/java/ApiServer.java

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.neo4j.driver.Record;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServer {
    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);

    public static void main(String[] args) throws IOException {
        // 从环境变量获取数据库连接信息（推荐）

        String uri = "bolt://1.94.25.252:7687";
        String user = "neo4j";
        String password = "zstp123456";
        Neo4jUtil.init(uri, user, password);

        HttpServer server = HttpServer.create(new InetSocketAddress(8083), 0);
        logger.info("服务器启动，监听端口 8083");

        server.createContext("/api/data", new DataHandler());

        // 使用固定大小的线程池，提高并发处理能力
        ExecutorService executor = Executors.newFixedThreadPool(10); // 10个线程
        server.setExecutor(executor);
        server.start();
    }

    /**
     * 处理 /api/data 请求的处理器
     */
    static class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                DetailRepository repository = new DetailRepository();
                List<DetailKnowledgePoint> knowledgePoints = repository.getAllDetailKnowledgePoints();

                // 构建响应数据
                List<ResultItem> resultItems = new ArrayList<>();
                for (DetailKnowledgePoint kp : knowledgePoints) {
                    ResultItem item = new ResultItem(
                            kp.getId(),
                            kp.getTags(),
                            kp.getPrePoint(),
                            kp.getPostPoint(),
                            kp.getTeachingObjective(),
                            kp.getRelatedPoint(),
                            kp.getCognition(),
                            kp.getContent()

                    );
                    resultItems.add(item);
                }

                ApiResponse response = new ApiResponse(200, "第二组", true, resultItems);

                // 将响应对象转换为JSON
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonResponse = gson.toJson(response);

                // 设置响应头和状态码
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);

                // 写入响应体
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }

    /**
     * 响应结构类
     */
    static class ApiResponse {
        private int code;
        private String owner;
        private boolean success;
        private List<ResultItem> result;

        public ApiResponse(int code, String owner, boolean success, List<ResultItem> result) {
            this.code = code;
            this.owner = owner;
            this.success = success;
            this.result = result;
        }

        // Getters 和 Setters（根据需要添加）
        public int getCode() { return code; }
        public String getOwner() { return owner; }
        public boolean isSuccess() { return success; }
        public List<ResultItem> getResult() { return result; }

        public void setCode(int code) { this.code = code; }
        public void setOwner(String owner) { this.owner = owner; }
        public void setSuccess(boolean success) { this.success = success; }
        public void setResult(List<ResultItem> result) { this.result = result; }
    }

    /**
     * 结果项类，使用单数命名以符合API文档
     */
    static class ResultItem {
        private String id;
        private List<String> tags;
        private List<String> prePoint;
        private List<String> postPoint;
        private List<String> teachingObjective;
        private List<String> relatedPoint;
        private List<String> cognition;
        private String content;

        public ResultItem(String id, List<String> tags, List<String> prePoint, List<String> postPoint,
                          List<String> teachingObjective, List<String> relatedPoint, List<String> cognition, String content) {
            this.id = id;
            this.tags = tags;
            this.prePoint = prePoint;
            this.postPoint = postPoint;
            this.teachingObjective = teachingObjective;
            this.relatedPoint = relatedPoint;
            this.cognition = cognition;
            this.content = content;
        }

        // Getters 和 Setters（根据需要添加）
        public String getId() { return id; }
        public List<String> getTags() { return tags; }
        public List<String> getPrePoint() { return prePoint; }
        public List<String> getPostPoint() { return postPoint; }
        public List<String> getTeachingObjective() { return teachingObjective; }
        public List<String> getRelatedPoint() { return relatedPoint; }
        public List<String> getCognition() { return cognition; }
        public String getContent() { return content; }

        public void setId(String id) { this.id = id; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public void setPrePoint(List<String> prePoint) { this.prePoint = prePoint; }
        public void setPostPoint(List<String> postPoint) { this.postPoint = postPoint; }
        public void setTeachingObjective(List<String> teachingObjective) { this.teachingObjective = teachingObjective; }
        public void setRelatedPoint(List<String> relatedPoint) { this.relatedPoint = relatedPoint; }
        public void setContent(String content) { this.content = content; }

    }

    /**
     * Neo4j工具类，用于初始化和管理驱动
     */
    static class Neo4jUtil {

        private static Driver driver;

        private Neo4jUtil() {}

        // 初始化驱动
        public static void init(String uri, String user, String password) {
            if (driver == null) {
                driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    driver.close();
                    logger.info("Neo4j 驱动已关闭");
                }));
                logger.info("Neo4j 驱动已初始化");
            }
        }

        // 获取会话
        public static Session getSession() {
            if (driver == null) {
                throw new IllegalStateException("Neo4j 驱动尚未初始化");
            }
            return driver.session();
        }
    }

    /**
     * 数据读取类，执行查询并映射结果
     */
    static class DataReader {

        private static final Logger logger = LoggerFactory.getLogger(DataReader.class);

        // 通用查询方法，接受查询语句和处理结果的函数
        public <T> List<T> executeQuery(String query, Function<Record, T> mapper) {
            List<T> results = new ArrayList<>();
            try (Session session = Neo4jUtil.getSession()) {
                Result result = session.run(query);

                while (result.hasNext()) {
                    Record record = result.next();
                    results.add(mapper.apply(record));
                }
            } catch (Exception e) {
                logger.error("执行查询时发生异常: " + query, e);
                // 根据需要，可以抛出自定义异常或返回一个空列表
            }
            return results;
        }

        // 带参数的查询方法
        public <T> List<T> executeQuery(String query, Map<String, Object> parameters, Function<Record, T> mapper) {
            List<T> results = new ArrayList<>();
            try (Session session = Neo4jUtil.getSession()) {
                Result result = session.run(query, parameters);

                while (result.hasNext()) {
                    Record record = result.next();
                    results.add(mapper.apply(record));
                }
            } catch (Exception e) {
                logger.error("执行带参数的查询时发生异常: " + query, e);
                // 根据需要，可以抛出自定义异常或返回一个空列表
            }
            return results;
        }
    }

    /**
     * Repository类，用于与数据库交互
     */
    static class DetailRepository {
        private final DataReader dataReader = new DataReader();

        /**
         * 获取所有知识点
         */
        public List<DetailKnowledgePoint> getAllDetailKnowledgePoints() {
            String query = "MATCH (kp:知识点) RETURN kp";
            return dataReader.executeQuery(query, mapToKnowledgePoint());
        }


        /**
         * 映射Record到DetailKnowledgePoint对象
         */
        private Function<Record, DetailKnowledgePoint> mapToKnowledgePoint() {
            return record -> {
                String id = record.get("kp").get("id").asString();
                String name = record.get("kp").get("name").asString();
                String content = record.get("kp").get("content").asString();

                // 可选字段
                List<String> cognition = record.get("kp").get("cognitive_dimension").isNull() ? null : record.get("kp").get("cognitive_dimension").asList( obj -> obj != null ? obj.toString() : null);
                List<String> tags = record.get("kp").get("tags").isNull() ? null : record.get("kp").get("tags").asList( obj -> obj != null ? obj.toString() : null);
                List<String> note = record.get("kp").get("teaching_objective").isNull() ? null : record.get("kp").get("teaching_objective").asList( obj -> obj != null ? obj.toString() : null);

                // 处理 pre_requisite 字段
                List<String> prePoints = new ArrayList<>();
                if (!record.get("kp").get("pre_requisite").isNull()) {
                    record.get("kp").get("pre_requisite").values().forEach(value -> {
                        String prePointId = value.asString();
                        prePoints.add(prePointId);
                    });
                }

                // 处理 post_requisite 字段
                List<String> postPoints = new ArrayList<>();
                if (!record.get("kp").get("post_requisite").isNull()) {
                    record.get("kp").get("post_requisite").values().forEach(value -> {
                        String postPointId = value.asString();
                        postPoints.add(postPointId);
                    });
                }

                // 处理 related_points 字段
                List<String> relatedPoints = new ArrayList<>();
                if (!record.get("kp").get("related_points").isNull()) {
                    record.get("kp").get("related_points").values().forEach(value -> {
                        String relatedPointId = value.asString();
                        relatedPoints.add(relatedPointId);
                    });
                }

                return new DetailKnowledgePoint(id, name, content, prePoints, postPoints, cognition, tags, relatedPoints, note);
            };
        }
    }

    /**
     * 模型类，表示详细的知识点
     */
    static class DetailKnowledgePoint {
        private String id;
        private String name;
        private String content;
        private List<String> prePoint;
        private List<String> postPoint;
        private List<String> cognition;
        private List<String> tags;
        private List<String> relatedPoint;
        private List<String> teachingObjective;

        public DetailKnowledgePoint(String id, String name, String content, List<String> prePoint,
                                    List<String> postPoint, List<String> cognition, List<String> tags,
                                    List<String> relatedPoint, List<String> teachingObjective) {
            this.id = id;
            this.name = name;
            this.content = content;
            this.prePoint = prePoint;
            this.postPoint = postPoint;
            this.cognition = cognition;
            this.tags = tags;
            this.relatedPoint = relatedPoint;
            this.teachingObjective = teachingObjective;
        }

        // Getters 和 Setters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getContent() { return content; }
        public List<String> getPrePoint() { return prePoint; }
        public List<String> getPostPoint() { return postPoint; }
        public List<String> getCognition() { return cognition; }
        public List<String> getTags() { return tags; }
        public List<String> getRelatedPoint() { return relatedPoint; }
        public List<String> getTeachingObjective() { return teachingObjective; }

        public void setId(String id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setContent(String content) { this.content = content; }
        public void setPrePoint(List<String> prePoint) { this.prePoint = prePoint; }
        public void setPostPoint(List<String> postPoint) { this.postPoint = postPoint; }
        public void setCognition(List<String> cognition) { this.cognition = cognition; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public void setRelatedPoint(List<String> relatedPoint) { this.relatedPoint = relatedPoint; }
        public void setTeachingObjective(List<String> teachingObjective) { this.teachingObjective = teachingObjective; }
    }
}
