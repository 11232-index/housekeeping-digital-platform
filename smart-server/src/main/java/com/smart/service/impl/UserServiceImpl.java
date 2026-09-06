package com.smart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.smart.constant.MessageConstant;
import com.smart.dto.UserLoginDTO;
import com.smart.entity.User;
import com.smart.exception.LoginFailedException;
import com.smart.mapper.UserMapper;
import com.smart.properties.WeChatProperties;
import com.smart.service.UserService;
import com.smart.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String code = userLoginDTO.getCode();

        // 调用微信接口服务，获取openid
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", paramMap);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 根据openid查询用户
        User user = userMapper.getByOpenid(openid);

        // 新用户自动注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }
}
