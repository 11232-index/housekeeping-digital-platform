package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.constant.MessageConstant;
import com.smart.dto.AddressBookPageQueryDTO;
import com.smart.entity.AddressBook;
import com.smart.exception.AddressBookBusinessException;
import com.smart.mapper.AddressBookMapper;
import com.smart.result.PageResult;
import com.smart.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 新增地址簿
     * @param addressBook
     * @param userId
     */
    @Override
    @Transactional
    @CacheEvict(value = "addressBook", allEntries = true)
    public void add(AddressBook addressBook, Long userId) {
        addressBook.setUserId(userId);

        // 如果当前是第一条地址，自动设为默认
        List<AddressBook> existingList = addressBookMapper.listByUserId(userId);
        if (existingList == null || existingList.isEmpty()) {
            addressBook.setIsDefault(1);
        } else {
            // 非第一条地址，默认不设为默认（isDefault = 0）
            // 如果前端明确指定了默认，则先清除其他默认地址
            if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
                addressBookMapper.clearDefaultByUserId(userId);
            } else {
                addressBook.setIsDefault(0);
            }
        }

        addressBookMapper.insert(addressBook);
        log.info("新增地址簿成功，用户id={}，地址簿id={}", userId, addressBook.getId());
    }

    /**
     * 根据id删除地址簿
     * @param id
     * @param userId
     */
    @Override
    @Transactional
    @CacheEvict(value = "addressBook", allEntries = true)
    public void deleteById(Long id, Long userId) {
        AddressBook addressBook = addressBookMapper.getById(id);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 校验地址是否属于当前用户
        if (!addressBook.getUserId().equals(userId)) {
            throw new AddressBookBusinessException("无权删除该地址");
        }

        addressBookMapper.deleteById(id);
        log.info("删除地址簿成功，用户id={}，地址簿id={}", userId, id);

        // 如果删除的是默认地址，自动将最新的一条地址设为默认
        if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
            List<AddressBook> remainingList = addressBookMapper.listByUserId(userId);
            if (remainingList != null && !remainingList.isEmpty()) {
                AddressBook firstAddress = remainingList.get(0);
                addressBookMapper.setDefault(firstAddress.getId(), userId);
                log.info("删除默认地址后，自动设置地址簿id={}为默认", firstAddress.getId());
            }
        }
    }

    /**
     * 修改地址簿
     * @param addressBook
     * @param userId
     */
    @Override
    @Transactional
    @CacheEvict(value = "addressBook", allEntries = true)
    public void update(AddressBook addressBook, Long userId) {
        // 校验地址是否存在
        AddressBook existing = addressBookMapper.getById(addressBook.getId());
        if (existing == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 校验地址是否属于当前用户
        if (!existing.getUserId().equals(userId)) {
            throw new AddressBookBusinessException("无权修改该地址");
        }

        // 保留 user_id 不变
        addressBook.setUserId(userId);

        // 如果修改为默认，先清除其他默认地址
        if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
            addressBookMapper.clearDefaultByUserId(userId);
        }

        addressBookMapper.update(addressBook);
        log.info("修改地址簿成功，用户id={}，地址簿id={}", userId, addressBook.getId());
    }

    /**
     * 根据id查询地址簿
     * @param id
     * @return
     */
    @Override
    @Cacheable(value = "addressBook", key = "'one:' + #id", sync = true)
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        return addressBook;
    }

    /**
     * 分页查询当前用户的地址簿列表
     * @param dto
     * @param userId
     * @return
     */
    @Override
    @Cacheable(value = "addressBook", key = "'page:' + #userId + ':' + #dto.page + ':' + #dto.pageSize + ':' + #dto.consignee", sync = true)
    public PageResult pageQuery(AddressBookPageQueryDTO dto, Long userId) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<AddressBook> page = addressBookMapper.pageQuery(dto, userId);
        log.info("分页查询地址簿，用户id={}，共{}条", userId, page.getTotal());
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 设置默认地址
     * @param id
     * @param userId
     */
    @Override
    @Transactional
    @CacheEvict(value = "addressBook", allEntries = true)
    public void setDefault(Long id, Long userId) {
        // 校验地址是否存在
        AddressBook existing = addressBookMapper.getById(id);
        if (existing == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 校验地址是否属于当前用户
        if (!existing.getUserId().equals(userId)) {
            throw new AddressBookBusinessException("无权操作该地址");
        }

        // 先清除所有默认地址，再设置指定地址为默认
        addressBookMapper.clearDefaultByUserId(userId);
        addressBookMapper.setDefault(id, userId);
        log.info("设置默认地址成功，用户id={}，地址簿id={}", userId, id);
    }

    /**
     * 查询默认地址
     * @param userId
     * @return
     */
    @Override
    @Cacheable(value = "addressBook", key = "'default:' + #userId", sync = true)
    public AddressBook getDefault(Long userId) {
        AddressBook addressBook = addressBookMapper.getDefaultByUserId(userId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        return addressBook;
    }

}
