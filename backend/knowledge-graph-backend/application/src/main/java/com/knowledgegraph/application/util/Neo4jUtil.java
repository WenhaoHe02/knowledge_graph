package com.knowledgegraph.application.util;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.GraphDatabase;

public class Neo4jUtil {

    private static Driver driver;
    private Neo4jUtil() {}

    // 构造函数，创建一个新的 Neo4j 驱动实例
    public static void init(String uri, String user, String password) {
        if (driver == null) {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        }
    }

    // 获取一个新的 Session
    public static Session getSession() {
        return driver.session();
    }

    // 关闭驱动
    public static void close() {
        driver.close();
    }
}
