package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.util.Neo4jUtil;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Values;

public class RegisterRepository {

    // 检查用户是否存在
    public boolean userExists(String userName) {
        String query = "MATCH (u:用户信息) WHERE u.userName = $userName RETURN u";
        try (Session session = Neo4jUtil.getSession()) {
            Result result = session.run(query, Values.parameters("userName", userName));
            return result.hasNext(); // 如果有结果，表示用户存在
        }
    }

    // 添加用户
    public void addUser(String userName, String password, String role) {
        String query = "CREATE (u:用户信息 {userName: $userName, password: $password, role: $role})";
        try (Session session = Neo4jUtil.getSession()) {
            session.run(query, Values.parameters(
                    "userName", userName,
                    "password", password,
                    "role", role
            ));
        }
    }

    // 更新用户角色
    public boolean updateUserRole(String userName, String newRole) {
        String query = "MATCH (u:用户信息) WHERE u.userName = $userName SET u.role = $newRole RETURN u";
        try (Session session = Neo4jUtil.getSession()) {
            Result result = session.run(query, Values.parameters(
                    "userName", userName,
                    "newRole", newRole
            ));
            return result.hasNext(); // 如果有结果，表示更新成功
        }
    }

    // 删除用户
    public boolean deleteUser(String userName) {
        String query = "MATCH (u:用户信息) WHERE u.userName = $userName DELETE u RETURN COUNT(u) AS deletedCount";
        try (Session session = Neo4jUtil.getSession()) {
            Result result = session.run(query, Values.parameters("userName", userName));
            return result.hasNext(); // 如果有结果，表示删除成功
        }
    }
}
