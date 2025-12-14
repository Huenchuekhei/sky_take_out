package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

import javax.security.auth.login.LoginException;

public interface UserService {


    User Wxlogin(UserLoginDTO userLoginDTO);

    void save(User user);
}
