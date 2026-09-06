package com.smart.service;

import com.smart.dto.UserLoginDTO;
import com.smart.entity.User;

public interface UserService {
    /**
     * 登录
     *
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);
}
