package com.knowledgegraph.application.util;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jUtil {

    // 定义驱动对象
    private static Driver driver;

    // 私有构造函数，防止实例化
    private Neo4jUtil() {}

    // 初始化驱动对象
    public static void init(String uri, String user, String password) {
        if (driver == null) {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        }
    }

    // 获取驱动对象
    public static Driver getDriver() {
        if (driver == null) {
            throw new IllegalStateException("Driver is not initialized. Call init() first.");
        }
        return driver;
    }

    // 关闭驱动
    public static void close() {
        if (driver != null) {
            driver.close();
        }
    }
}
