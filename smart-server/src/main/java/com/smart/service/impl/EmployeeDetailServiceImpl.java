package com.smart.service.impl;

import com.smart.dto.EmployeeDetailDTO;
import com.smart.entity.EmployeeDetail;
import com.smart.mapper.EmployeeDetailMapper;
import com.smart.service.EmployeeDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EmployeeDetailServiceImpl implements EmployeeDetailService {

    @Autowired
    private EmployeeDetailMapper employeeDetailMapper;

    /**
     * 根据员工ID查询详细信息
     *
     * @param employeeId
     * @return
     */
    @Override
    public EmployeeDetailDTO getByEmployeeId(Long employeeId) {
        EmployeeDetail detail = employeeDetailMapper.getByEmployeeId(employeeId);
        if (detail == null) {
            return null;
        }
        EmployeeDetailDTO dto = new EmployeeDetailDTO();
        BeanUtils.copyProperties(detail, dto);
        if (detail.getUpdateTime() != null) {
            dto.setUpdateTime(detail.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    /**
     * 新增员工详细信息
     *
     * @param employeeDetailDTO
     */
    @Override
    @CacheEvict(value = "housekeeperEmployee", allEntries = true)
    public void save(EmployeeDetailDTO employeeDetailDTO) {
        EmployeeDetail detail = new EmployeeDetail();
        BeanUtils.copyProperties(employeeDetailDTO, detail);
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());
        employeeDetailMapper.insert(detail);
        log.info("新增员工详细信息成功，employeeId：{}", employeeDetailDTO.getEmployeeId());
    }

    /**
     * 修改员工详细信息
     *
     * @param employeeDetailDTO
     */
    @Override
    @CacheEvict(value = "housekeeperEmployee", allEntries = true)
    public void update(EmployeeDetailDTO employeeDetailDTO) {
        EmployeeDetail detail = new EmployeeDetail();
        BeanUtils.copyProperties(employeeDetailDTO, detail);
        detail.setUpdateTime(LocalDateTime.now());
        employeeDetailMapper.update(detail);
        log.info("修改员工详细信息成功，employeeId：{}", employeeDetailDTO.getEmployeeId());
    }

    /**
     * 根据员工ID删除详细信息
     *
     * @param employeeId
     */
    @Override
    @CacheEvict(value = "housekeeperEmployee", allEntries = true)
    public void deleteByEmployeeId(Long employeeId) {
        employeeDetailMapper.deleteByEmployeeId(employeeId);
        log.info("删除员工详细信息成功，employeeId：{}", employeeId);
    }
}
