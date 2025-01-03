package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.util.DataReader;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;

import java.util.function.Function;

public class RoleRepository {
    private final DataReader dataReader = new DataReader();

    public String getRoleForUser(String userName, String password) {
        String query = "MATCH (u:用户信息) WHERE u.userName = $userName AND u.password = $password RETURN u.role AS role";

        var results = dataReader.executeQuery(query,
                Values.parameters("userName", userName, "password", password),
                mapToRole()
        );

        return results.isEmpty() ? null : results.get(0);
    }

    private Function<Record, String> mapToRole() {
        return record -> record.get("role").asString();
    }
}
