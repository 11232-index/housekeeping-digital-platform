package com.smart.mapper;

import com.smart.dto.AddressBookPageQueryDTO;
import com.smart.entity.AddressBook;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AddressBookMapper {

    /**
     * 新增地址簿
     * @param addressBook
     */
    void insert(AddressBook addressBook);

    /**
     * 根据id删除地址簿
     * @param id
     */
    void deleteById(Long id);

    /**
     * 修改地址簿
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 根据id查询地址簿
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 分页查询地址簿
     * @param dto
     * @param userId
     * @return
     */
    Page<AddressBook> pageQuery(@Param("dto") AddressBookPageQueryDTO dto, @Param("userId") Long userId);

    /**
     * 根据用户id查询所有地址簿列表
     * @param userId
     * @return
     */
    java.util.List<AddressBook> listByUserId(Long userId);

    /**
     * 将用户所有地址设为非默认
     * @param userId
     */
    void clearDefaultByUserId(Long userId);

    /**
     * 设置默认地址
     * @param id
     * @param userId
     */
    void setDefault(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询默认地址
     * @param userId
     * @return
     */
    AddressBook getDefaultByUserId(Long userId);

}
