package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.KnowledgePoint;
import org.neo4j.driver.Values;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import java.util.ArrayList;
import java.util.List;
import com.knowledgegraph.application.util.Neo4jUtil;
import java.util.function.Function;
public class SearchRepository {


    // 使用Neo4jUtil获取Driver
    private final DataReader dataReader = new DataReader();

    // 根据关键字搜索知识点
    public List<KnowledgePoint> searchKnowledgePoints(String keyword) {
        String query = "MATCH (kp:知识点) WHERE kp.name CONTAINS $keyword RETURN kp";
        return dataReader.executeQuery(query, Values.parameters("keyword", keyword), mapToKnowledgePoint());
    }

    // 获取所有知识点
    public List<KnowledgePoint> getAllKnowledgePoints() {
        String query = "MATCH (kp:知识点) RETURN kp";
        return dataReader.executeQuery(query, mapToKnowledgePoint());
    }

    // 将查询结果映射为 KnowledgePoint 对象的函数
    private Function<Record, KnowledgePoint> mapToKnowledgePoint() {
        return record -> {
            KnowledgePoint kp = new KnowledgePoint();
            kp.setId(record.get("kp").get("id").asString());
            kp.setName(record.get("kp").get("name").asString());
            kp.setContent(record.get("kp").get("content").asString());
            return kp;
        };
    }
    static  class DataReader {

        // 通用查询方法，接受查询语句和处理结果的函数
        public <T> List<T> executeQuery(String query, Function<Record, T> mapper) {
            List<T> results = new ArrayList<>();
            try (Session session = Neo4jUtil.getSession()) {
                Result result = session.run(query);

                while (result.hasNext()) {
                    Record record = result.next();
                    results.add(mapper.apply(record));
                }
            }
            return results;
        }

        // 带参数的查询方法
        public <T> List<T> executeQuery(String query, org.neo4j.driver.Value parameters, Function<Record, T> mapper) {
            List<T> results = new ArrayList<>();
            try (Session session = Neo4jUtil.getSession()) {
                Result result = session.run(query, parameters);

                while (result.hasNext()) {
                    Record record = result.next();
                    results.add(mapper.apply(record));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return results;
        }
    }
}

