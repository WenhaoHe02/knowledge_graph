package com.knowledgegraph.application;

import com.knowledgegraph.application.controller.DetailController;
import com.knowledgegraph.application.controller.QAController;
import com.knowledgegraph.application.util.Neo4jUtil;
import com.sun.net.httpserver.HttpServer;
import com.knowledgegraph.application.controller.SearchController;
import com.knowledgegraph.application.controller.ExerciseController;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        String uri = "bolt://1.94.25.252:7687";
        String user = "neo4j";
        String password = "zstp123456";
        Neo4jUtil.init(uri, user, password);
        System.out.println("Neo4j 数据库连接已初始化");

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8083), 0);
            SearchController.registerEndpoints(server);
            DetailController.registerEndpoints(server);
            QAController.registerEndpoints(server);
            ExerciseController.registerEndpoints(server); // 注册 Exercise 接口
            server.setExecutor(null); // 使用默认的线程池
            server.start();
            System.out.println("HTTP 服务器已启动，监听端口 8083");
        } catch (IOException e) {
            e.printStackTrace();
            Neo4jUtil.close();
            System.out.println("HTTP 服务器启动失败");
            return;
        }

        // 添加关闭钩子以在程序结束时关闭 Neo4j 驱动
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Neo4jUtil.close();
            System.out.println("Neo4j 数据库连接已关闭");
        }));
    }
}
