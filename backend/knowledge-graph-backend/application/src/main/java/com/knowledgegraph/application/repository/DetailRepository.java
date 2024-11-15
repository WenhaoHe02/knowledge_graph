package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.DetailKnowledgePoint;
import com.knowledgegraph.application.util.DataReader;
import org.neo4j.driver.Values;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class DetailRepository {
    private final DataReader dataReader = new DataReader();

    public DetailKnowledgePoint searchDetailKnowledgePoints(String id) {
        String query = "MATCH (kp:知识点) WHERE kp.id = $id RETURN kp";
        return dataReader.executeQuery(query, Values.parameters("id", id), mapToKnowledgePoint()).get(0);

    }
    private Function<Record, DetailKnowledgePoint> mapToKnowledgePoint() {
        return record -> {
            // 提取基本字段
            String id = record.get("kp").get("id").asString();
            String name = record.get("kp").get("name").asString();
            String content = record.get("kp").get("content").asString();

            // 可选字段
            String cognition = record.get("kp").get("cognitive_dimension").isNull() ? null : record.get("kp").get("cognitive_dimension").get(0).asString();
            String tag = record.get("kp").get("tags").isNull() ? null : record.get("kp").get("tags").get(0).asString();
            String note = record.get("kp").get("teaching_objective").isNull() ? null : record.get("kp").get("teaching_objective").get(0).asString();

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

            // 创建并返回 DetailKnowledgePoint 对象
            return new DetailKnowledgePoint(id, name, content, prePoints, postPoints, cognition, tag, relatedPoints, note);
        };
    }

}

