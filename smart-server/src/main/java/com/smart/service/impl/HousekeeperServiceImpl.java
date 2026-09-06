package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.dto.HousekeeperDTO;
import com.smart.dto.HousekeeperPageQueryDTO;
import com.smart.entity.Employee;
import com.smart.entity.EmployeeDetail;
import com.smart.entity.Housekeeper;
import com.smart.mapper.EmployeeDetailMapper;
import com.smart.mapper.EmployeeMapper;
import com.smart.mapper.HousekeeperMapper;
import com.smart.result.PageResult;
import com.smart.service.HousekeeperService;
import com.smart.vo.HousekeeperEmployeeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HousekeeperServiceImpl implements HousekeeperService {

    @Autowired
    private HousekeeperMapper housekeeperMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private EmployeeDetailMapper employeeDetailMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "housekeeper", allEntries = true),
            @CacheEvict(value = "housekeeperEmployee", allEntries = true)
    })
    public void save(HousekeeperDTO dto) {
        Housekeeper h = new Housekeeper();
        BeanUtils.copyProperties(dto, h);
        housekeeperMapper.insert(h);
        log.info("新增家政员，id={}, name={}", h.getId(), h.getName());
    }

    @Override
    @Cacheable(value = "housekeeper", key = "'page:' + #dto.page + ':' + #dto.pageSize + ':' + #dto.name", sync = true)
    public PageResult pageQuery(HousekeeperPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Housekeeper> page = housekeeperMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Cacheable(value = "housekeeper", key = "'one:' + #id", sync = true)
    public Housekeeper getById(Long id) {
        return housekeeperMapper.getById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "housekeeper", allEntries = true),
            @CacheEvict(value = "housekeeperEmployee", allEntries = true)
    })
    public void update(HousekeeperDTO dto) {
        Housekeeper h = new Housekeeper();
        BeanUtils.copyProperties(dto, h);
        housekeeperMapper.update(h);
        log.info("修改家政员，id={}", h.getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "housekeeper", allEntries = true),
            @CacheEvict(value = "housekeeperEmployee", allEntries = true)
    })
    public void deleteByEmployeeId(Long employeeId) {
        housekeeperMapper.deleteByEmployeeId(employeeId);
        log.info("删除家政员，employeeId={}", employeeId);
    }

    /**
     * 根据家政员ID查询关联的员工信息（含员工详细信息）
     *
     * 查询流程（由 Spring Cache + Redis 自动管理）：
     * 1. 先查 Redis 缓存，命中则直接返回
     * 2. 缓存未命中 → 查 MySQL：
     *    a. 根据 housekeeperId 查询家政员表，获取 employeeId
     *    b. 根据 employeeId 查询员工表（Employee），获取基础信息
     *    c. 根据 employeeId 查询员工详情表（EmployeeDetail），获取详细信息
     *    d. 组装 HousekeeperEmployeeVO 并返回
     * 3. 查询结果自动写入 Redis（缓存名: housekeeperEmployee，key: 家政员ID）
     * 4. 后续相同 housekeeperId 的请求直接从 Redis 返回，无需访问 MySQL
     *
     * 缓存失效：
     * - TTL 到期自动淘汰（默认 30 分钟）
     * - 员工信息或员工详情修改时，需手动调用 cacheManager 清除对应缓存
     *
     * @param housekeeperId 家政员ID
     * @return 员工信息 + 详细信息 VO，若家政员不存在则返回 null
     */
    @Override
    @Cacheable(value = "housekeeperEmployee", key = "#housekeeperId", sync = true)
    public HousekeeperEmployeeVO getEmployeeByHousekeeperId(Long housekeeperId) {
        log.info("=== 查询家政员关联员工信息：housekeeperId={}（缓存未命中，查 MySQL）===", housekeeperId);

        // 1. 查询家政员，获取关联的 employeeId
        Housekeeper housekeeper = housekeeperMapper.getById(housekeeperId);
        if (housekeeper == null) {
            log.warn("家政员不存在：housekeeperId={}", housekeeperId);
            return null;
        }
        Long employeeId = housekeeper.getEmployeeId();
        if (employeeId == null) {
            log.warn("家政员未关联员工：housekeeperId={}", housekeeperId);
            return null;
        }

        // 2. 查询员工表，获取基础信息
        Employee employee = employeeMapper.getById(employeeId);
        if (employee == null) {
            log.warn("员工不存在：employeeId={}", employeeId);
            return null;
        }

        // 3. 查询员工详情表，获取详细信息（可能为 null，员工不一定有详情记录）
        EmployeeDetail detail = employeeDetailMapper.getByEmployeeId(employeeId);

        // 4. 组装 VO
        HousekeeperEmployeeVO vo = HousekeeperEmployeeVO.builder()
                // 员工基础信息
                .employeeId(employee.getId())
                .name(employee.getName())
                .phone(employee.getPhone())
                .sex(employee.getSex())
                .age(employee.getAge())
                // 员工详细信息（允许为 null）
                .education(detail != null ? detail.getEducation() : null)
                .pastExperience(detail != null ? detail.getPastExperience() : null)
                .position(detail != null ? detail.getPosition() : null)
                .workStatus(detail != null ? detail.getWorkStatus() : null)
                .healthCertificate(detail != null ? detail.getHealthCertificate() : null)
                .build();

        log.info("查询成功：housekeeperId={}, employeeId={}, name={}, position={}",
                housekeeperId, employeeId, employee.getName(),
                detail != null ? detail.getPosition() : "无");
        return vo;
    }

}
