package com.smart.controller.user;

import com.smart.context.BaseContext;
import com.smart.dto.ShoppingCartDTO;
import com.smart.entity.ServiceRequirement;
import com.smart.entity.ShoppingCart;
import com.smart.result.Result;
import com.smart.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端 - 购物车相关接口
 */
@RestController
@RequestMapping("/user/shopping-cart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 新增购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("新增购物车，用户id={}：{}", userId, shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO, userId);
        return Result.success();
    }

    /**
     * 查询购物车列表
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询购物车列表，用户id={}", userId);
        List<ShoppingCart> cartList = shoppingCartService.list(userId);
        return Result.success(cartList);
    }

    /**
     * 减少购物车数量（数量>1减1，数量=1删除）
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("减少购物车，用户id={}：{}", userId, shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO, userId);
        return Result.success();
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result clean() {
        Long userId = BaseContext.getCurrentId();
        log.info("清空购物车，用户id={}", userId);
        shoppingCartService.clean(userId);
        return Result.success();
    }

    /**
     * 查询服务需求（根据服务项目id或服务套餐id）
     * @param serviceItemId
     * @param servicePackageId
     * @return
     */
    @GetMapping("/requirements")
    public Result<List<ServiceRequirement>> getRequirements(
            @RequestParam(required = false) Long serviceItemId,
            @RequestParam(required = false) Long servicePackageId) {
        log.info("查询服务需求，serviceItemId={}，servicePackageId={}", serviceItemId, servicePackageId);
        List<ServiceRequirement> list = shoppingCartService.getRequirements(serviceItemId, servicePackageId);
        return Result.success(list);
    }
}
