package com.knowledgegraph.application.service;

import com.knowledgegraph.application.repository.RoleRepository;

public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl() {
        this.roleRepository = new RoleRepository();
    }

    @Override
    public boolean validateRole(String userName, String password, String role) {
        // 从数据库中查询用户
        String storedRole = roleRepository.getRoleForUser(userName, password);

        return storedRole != null && storedRole.equalsIgnoreCase(role);
    }
}

