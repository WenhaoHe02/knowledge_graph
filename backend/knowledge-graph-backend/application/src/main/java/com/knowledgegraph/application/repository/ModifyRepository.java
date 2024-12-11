package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.DetailKnowledgePoint;
import com.knowledgegraph.application.util.Neo4jUtil;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import java.util.List;

public class ModifyRepository {

    private static final Driver driver = Neo4jUtil.getDriver();

    public static String modifyKnowledgePoint(DetailKnowledgePoint params) {
        String query = """
    MERGE (kp:知识点 {id: $id}) 
    SET kp.name = $name,
        kp.content = $content,
        kp.cognitive_dimension = $cognition, // 数组类型
        kp.tags = $tags,                     // 数组类型
        kp.teaching_objective = $note,       // 数组类型
        kp.pre_requisite = $prePoint,        // 数组类型
        kp.post_requisite = $postPoint,      // 数组类型
        kp.related_points = $relatedPoint    // 数组类型
    RETURN kp
""";

        try (Session session = driver.session()) {
            Result result = session.run(query, Values.parameters(
                    "id", params.getId(), // 唯一标识符
                    "name", params.getName(),
                    "content", params.getContent(),
                    // 将这些字段转换为数组
                    "cognition", convertToArray(params.getCognition()),
                    "tags", convertToArray(params.getTag()),
                    "note", convertToArray(params.getNote()),
                    "prePoint", convertToArray(params.getPrePoint()),
                    "postPoint", convertToArray(params.getPostPoint()),
                    "relatedPoint", convertToArray(params.getRelatedPoint())
            ));

            if (result.hasNext()) {
                Record record = result.next();
                return "Updated or Created Knowledge Point: " + record.get("kp").asMap();
            } else {
                return "No operation was performed.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

    }
    private static String[] convertToArray(Object field) {
        if (field == null) {
            return new String[]{};
        } else if (field instanceof String) {
            String str = (String) field;
            if (str.contains(",")) {
                return str.split(","); // 按逗号拆分
            } else {
                return new String[]{str}; // 普通字符串包装为数组
            }
        } else if (field instanceof List) {
            // 如果是 List 类型，直接转为数组
            List<?> list = (List<?>) field;
            return list.stream().map(Object::toString).toArray(String[]::new);
        } else {
            // 如果是其他类型，直接包装为数组
            return new String[]{field.toString()};
        }
    }
}
