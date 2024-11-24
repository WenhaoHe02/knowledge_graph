package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.KnowledgePoint;
import com.knowledgegraph.application.util.DataReader;
import com.knowledgegraph.application.util.Neo4jUtil;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;
import org.neo4j.driver.Record;
import java.util.List;
import java.util.function.Function;
public class SearchRepository {

    private final DataReader dataReader = new DataReader();

    // 根据关键字搜索知识点
    public List<KnowledgePoint> searchKnowledgePoints(String keyword) {
        String query = "MATCH (kp:知识点) WHERE kp.name CONTAINS $keyword RETURN kp";
        return dataReader.executeQuery(query, Values.parameters("keyword", keyword), mapToKnowledgePoint());
    }

    private Function<Record, KnowledgePoint> mapToKnowledgePoint() {
        return record -> {
            KnowledgePoint kp = new KnowledgePoint();
            kp.setId(record.get("kp").get("id").asString());
            kp.setName(record.get("kp").get("name").asString());
            kp.setContent(record.get("kp").get("content").asString());
            return kp;
        };
    }
}

