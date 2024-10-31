package org.example;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

public class Neo4jTest {

    // 定义驱动对象
    private final Driver driver;

    // 构造函数，连接 Neo4j 数据库
    public Neo4jTest(String uri, String user, String password) {
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

    public static void main(String[] args) {
        // 替换为你的数据库 URI、用户名和密码
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "zstp123456";

        Neo4jTest neo4jTest = new Neo4jTest(uri, user, password);

        // 创建节点
        neo4jTest.createNode();

        // 查询节点
        neo4jTest.searchNodes();

        // 关闭连接
        neo4jTest.close();
    }
}
