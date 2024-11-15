package com.knowledgegraph.application.util;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DataReader {

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