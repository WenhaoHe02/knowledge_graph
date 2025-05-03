package org.example;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EDataReader {

    /**
     * 不带参数的查询方法（不推荐使用带$参数的Cypher）
     */
    public <T> List<T> executeQuery(String query, Function<Record, T> mapper) {
        List<T> results = new ArrayList<>();
        try (Session session = Neo4jUtil.getSession()) {
            Result result = session.run(query);
            while (result.hasNext()) {
                Record record = result.next();
                results.add(mapper.apply(record));
            }
        } catch (Exception e) {
            System.err.println("[EDataReader] 查询失败（无参数）: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 带参数查询方法：适用于包含 $param 的 Cypher 查询
     */
    public <T> List<T> executeQuery(String query, Map<String, Object> parameters, Function<Record, T> mapper) {
        List<T> results = new ArrayList<>();
        try (Session session = Neo4jUtil.getSession()) {
            Value neo4jParams = Values.value(parameters);
            Result result = session.run(query, neo4jParams);

            while (result.hasNext()) {
                Record record = result.next();
                results.add(mapper.apply(record));
            }
        } catch (Exception e) {
            System.err.println("[EDataReader] 查询失败（带参数）: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }
}
