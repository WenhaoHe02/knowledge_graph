package com.knowledgegraph.application.service;

import com.knowledgegraph.application.repository.RegisterRepository;

public class RegisterServiceImpl implements RegisterService{
    private RegisterRepository registerRepository = new RegisterRepository();
    @Override
    public boolean registerUser(String userName, String password, String role) {
        if (registerRepository.userExists(userName)) {
            return false; // 用户已存在
        }
        registerRepository.addUser(userName, password, role); // 添加用户
        return true;
    }
}
