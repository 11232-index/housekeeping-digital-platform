package com.smart.controller.admin;

import com.smart.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminShopControllor")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopControllor {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置营业状态
     * @param status
     * @ return
     */
    @RequestMapping("/setStatus/{status}")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置营业状态：{}", status == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
        return Result.success();
    }

    /**
     * 获取营业状态
     * @return
     */
    @RequestMapping("/getStatus")
    public Result getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        // Redis 中无 SHOP_STATUS 数据时（首次启动/缓存被清空），默认营业中，避免 NPE
        if (status == null) {
            status = 1;
        }
        log.info("获取营业状态：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
