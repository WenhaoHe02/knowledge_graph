package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.KnowledgePoint;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import java.util.ArrayList;
import java.util.List;
import com.knowledgegraph.application.util.Neo4jUtil;
public class KnowledgeRepository {

    // 使用Neo4jUtil获取Driver
    private final Driver driver = Neo4jUtil.getDriver();

    public List<KnowledgePoint> searchKnowledgePoints(String keyword) {
        List<KnowledgePoint> knowledgePoints = new ArrayList<>();
        try (Session session = driver.session()) {
            Result result = session.run(
                    "MATCH (kp:KnowledgePoint) WHERE kp.name CONTAINS $keyword RETURN kp",
                    org.neo4j.driver.Values.parameters("keyword", keyword)
            );
            while (result.hasNext()) {
                Record record = result.next();
                KnowledgePoint kp = new KnowledgePoint();
                kp.setId(record.get("kp").get("id").asString());
                kp.setName(record.get("kp").get("name").asString());
                kp.setIntroduction(record.get("kp").get("introduction").asString());
                knowledgePoints.add(kp);
            }
        }
        return knowledgePoints;
    }
}

