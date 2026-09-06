package com.smart.controller.employee;

import com.smart.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("employeeShopControllor")
@RequestMapping("/employee/shop")
@Slf4j
public class ShopControllor {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取营业状态
     * @return
     */
    @RequestMapping("/get")
    public Result get() {
        log.info("获取营业状态");
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        return Result.success(status);
    }
}
