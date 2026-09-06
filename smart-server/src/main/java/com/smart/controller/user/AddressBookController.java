package com.smart.controller.user;

import com.smart.context.BaseContext;
import com.smart.dto.AddressBookPageQueryDTO;
import com.smart.entity.AddressBook;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * C端 - 地址簿相关接口
 */
@RestController
@RequestMapping("/user/address-book")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址簿
     * @param addressBook
     * @return
     */
    @PostMapping
    public Result add(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        log.info("新增地址簿，用户id={}：{}", userId, addressBook);
        addressBookService.add(addressBook, userId);
        return Result.success();
    }

    /**
     * 分页查询地址列表
     * @param dto
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(AddressBookPageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("分页查询地址簿，用户id={}：{}", userId, dto);
        PageResult pageResult = addressBookService.pageQuery(dto, userId);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询地址簿
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id) {
        log.info("查询地址簿，id={}", id);
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    /**
     * 修改地址簿
     * @param addressBook
     * @return
     */
    @PutMapping
    public Result update(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改地址簿，用户id={}：{}", userId, addressBook);
        addressBookService.update(addressBook, userId);
        return Result.success();
    }

    /**
     * 根据id删除地址簿
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除地址簿，用户id={}，地址簿id={}", userId, id);
        addressBookService.deleteById(id, userId);
        return Result.success();
    }

    /**
     * 设置默认地址
     * @param id
     * @return
     */
    @PutMapping("/default/{id}")
    public Result setDefault(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("设置默认地址，用户id={}，地址簿id={}", userId, id);
        addressBookService.setDefault(id, userId);
        return Result.success();
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    public Result<AddressBook> getDefault() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询默认地址，用户id={}", userId);
        AddressBook addressBook = addressBookService.getDefault(userId);
        return Result.success(addressBook);
    }

}
