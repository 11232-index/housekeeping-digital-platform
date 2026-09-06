package com.smart.service;

import com.smart.dto.AddressBookPageQueryDTO;
import com.smart.entity.AddressBook;
import com.smart.result.PageResult;

import java.util.List;

public interface AddressBookService {

    /**
     * 新增地址簿
     * @param addressBook
     * @param userId
     */
    void add(AddressBook addressBook, Long userId);

    /**
     * 根据id删除地址簿
     * @param id
     * @param userId
     */
    void deleteById(Long id, Long userId);

    /**
     * 修改地址簿
     * @param addressBook
     * @param userId
     */
    void update(AddressBook addressBook, Long userId);

    /**
     * 根据id查询地址簿
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 分页查询当前用户的地址簿列表
     * @param dto
     * @param userId
     * @return
     */
    PageResult pageQuery(AddressBookPageQueryDTO dto, Long userId);

    /**
     * 设置默认地址
     * @param id
     * @param userId
     */
    void setDefault(Long id, Long userId);

    /**
     * 查询默认地址
     * @param userId
     * @return
     */
    AddressBook getDefault(Long userId);

}
