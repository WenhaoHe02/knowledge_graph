package com.knowledgegraph.application;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

public class ConnectDataBase {

    // 定义驱动对象
    private final Driver driver;

    // 构造函数，连接 Neo4j 数据库
    public ConnectDataBase(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    // 关闭驱动
    public void close() {
        driver.close();
    }

    // 创建节点方法
    public void createNode() {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (n:Person {name: 'River', message: 'Hello, World!'})");
                return null;
            });
            System.out.println("节点已创建！");
        }
    }

    // 查询节点方法
    public void searchNodes() {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n:Person) RETURN n.name AS name, n.message AS message");

            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("Name: " + record.get("name").asString() + ", Message: " + record.get("message").asString());
            }
        }
    }


}
