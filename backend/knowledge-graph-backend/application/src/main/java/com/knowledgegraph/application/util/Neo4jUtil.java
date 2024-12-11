package com.knowledgegraph.application.util;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.GraphDatabase;

public class Neo4jUtil {

    private static Driver driver;
    private Neo4jUtil() {}

    public static void init(String uri, String user, String password) {
        if (driver == null) {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        }
    }

    public static Session getSession() {
        return driver.session();
    }

    public static void close() {
        driver.close();
    }

    public static Driver getDriver() {
        return driver;
    }
}
