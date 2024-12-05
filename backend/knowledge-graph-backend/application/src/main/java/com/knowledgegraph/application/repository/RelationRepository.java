package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.Relation;
import com.knowledgegraph.application.util.DataReader;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RelationRepository {
    // 关系映射
    private static final String PRE_RELATION = "pre_relation";
    private static final String POST_RELATION = "post_relation";
    private static final String INCLUDE_RELATION = "include_relation";

    private final DataReader dataReader = new DataReader();  // 使用 DataReader 处理与数据库的交互

    public List<Relation> findAllRelations() {
        String query = "MATCH (a)-[r:pre_relation|post_relation|include_relation]->(b) " +
                "RETURN a.id AS pointA, b.id AS pointB, type(r) AS relation";

        // 执行查询并转换为 Relation 对象列表
        return dataReader.executeQuery(query, null, mapToRelation());
    }

    private Function<Record, Relation> mapToRelation() {
        return record -> {
            // 提取关系数据
            String pointA = record.get("pointA").asString();
            String pointB = record.get("pointB").asString();
            String relation = record.get("relation").asString();
// 根据实际的数据库关系类型转换为返回的枚举值
            String mappedRelation = mapRelation(relation);

            // 如果映射后关系无效，则返回 null
            if (mappedRelation == null) {
                return null;  // 或者直接返回 null，表示忽略该记录
            }

            // 创建并返回 Relation 对象
            return new Relation(pointA, pointB, mappedRelation);
        };
    }

    private String mapRelation(String relation) {
        switch (relation) {
            case PRE_RELATION:
                return "pre";    // 映射为 "pre"
            case POST_RELATION:
                return "post";   // 映射为 "post"
            case INCLUDE_RELATION:
                return "parent"; // 映射为 "parent" 或 "child"（根据需求选择）
            default:
                return null;     // 如果关系类型不是预定义的，则返回 null
        }
    }
}
