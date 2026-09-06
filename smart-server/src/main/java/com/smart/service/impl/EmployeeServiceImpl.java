package com.smart.service.impl;

import com.github.pagehelper.PageHelper;
import com.smart.dto.EmployeeDTO;
import com.smart.dto.EmployeePageQueryDTO;
import com.smart.entity.Employee;
import com.smart.mapper.EmployeeDetailMapper;
import com.smart.mapper.EmployeeMapper;
import com.smart.mapper.HousekeeperMapper;
import com.smart.entity.Housekeeper;
import com.smart.result.PageResult;
import com.smart.service.EmployeeService;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private EmployeeDetailMapper employeeDetailMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private HousekeeperMapper housekeeperMapper;

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        BeanUtils.copyProperties(employeeDTO, employee);
        // 使用管理员设定的密码并BCrypt加密，未设定则默认change-me-password
        String rawPassword = (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isEmpty())
                ? employeeDTO.getPassword() : "change-me-password";
        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.setStatus(com.smart.constant.StatusConstant.ENABLE);
        employeeMapper.insert(employee);

        // 如果是家政员，自动创建家政员记录
        if ("cleaner".equals(employee.getRole())) {
            Housekeeper h = Housekeeper.builder()
                    .employeeId(employee.getId())
                    .name(employee.getName())
                    .price(BigDecimal.ZERO)
                    .rating(BigDecimal.ZERO)
                    .rank(0)
                    .carousel("")
                    .review("")
                    .build();
            housekeeperMapper.insert(h);
            clearHousekeeperCache();
            log.info("新增员工同时创建家政员记录，employeeId={}", employee.getId());
        }
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 开始进行分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        // updateTime、updateUser 由 AOP 自动填充，无需手动设置
        employeeMapper.startOrStop(employee);
    }

    /**
     * 根据ID查询员工信息
     *
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        return employeeMapper.getById(id);
    }

    /**
     * 修改员工信息
     *
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        // 先查旧数据，用于判断角色是否变更
        Employee existing = employeeMapper.getById(employeeDTO.getId());
        String oldRole = existing != null ? existing.getRole() : null;
        String newRole = employeeDTO.getRole();

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        } else {
            employee.setPassword(null);
        }
        employeeMapper.update(employee);

        // 角色变更：不是cleaner → cleaner，新增家政员记录
        if (!"cleaner".equals(oldRole) && "cleaner".equals(newRole)) {
            Housekeeper h = Housekeeper.builder()
                    .employeeId(employeeDTO.getId())
                    .name(employeeDTO.getName())
                    .price(BigDecimal.ZERO)
                    .rating(BigDecimal.ZERO)
                    .rank(0)
                    .carousel("")
                    .review("")
                    .build();
            housekeeperMapper.insert(h);
            clearHousekeeperCache();
            log.info("员工角色变更为家政员，已创建家政员记录，employeeId={}", employeeDTO.getId());
        }
        // 角色变更：cleaner → 不是cleaner，删除家政员记录
        if ("cleaner".equals(oldRole) && !"cleaner".equals(newRole)) {
            housekeeperMapper.deleteByEmployeeId(employeeDTO.getId());
            clearHousekeeperCache();
            log.info("员工角色从家政员变更，已删除家政员记录，employeeId={}", employeeDTO.getId());
        }
        // 如果一直是cleaner且名字变了，同步更新家政员名字
        if ("cleaner".equals(oldRole) && "cleaner".equals(newRole) && employeeDTO.getName() != null) {
            Housekeeper h = housekeeperMapper.getByEmployeeId(employeeDTO.getId());
            if (h != null) {
                h.setName(employeeDTO.getName());
                housekeeperMapper.update(h);
                clearHousekeeperCache();
            }
        }
    }

    /** 清空家政员缓存 */
    private void clearHousekeeperCache() {
        if (cacheManager.getCache("housekeeper") != null) {
            cacheManager.getCache("housekeeper").clear();
        }
        // housekeeperEmployee 缓存了员工信息（姓名/职位/健康证等），员工信息变更时同样需要清空，避免 C 端读到旧数据
        if (cacheManager.getCache("housekeeperEmployee") != null) {
            cacheManager.getCache("housekeeperEmployee").clear();
        }
    }

    /**
     * 删除员工信息
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        log.info("删除员工信息，员工ID: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }
        // 先删除员工详细信息（存在则删，不存在也不报错）
        employeeDetailMapper.deleteByEmployeeId(id);
        // 删除家政员记录（存在则删，不存在也不报错）
        housekeeperMapper.deleteByEmployeeId(id);
        clearHousekeeperCache();
        employeeMapper.delete(id);
        log.info("删除员工信息成功");
    }
}
