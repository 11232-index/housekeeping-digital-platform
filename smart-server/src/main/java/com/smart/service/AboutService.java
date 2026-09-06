package com.smart.service;

import com.smart.dto.AboutDTO;
import com.smart.dto.AboutPageQueryDTO;
import com.smart.entity.About;
import com.smart.result.PageResult;

public interface AboutService {

    void save(AboutDTO aboutDTO);

    PageResult pageQuery(AboutPageQueryDTO dto);

    About getById(Long id);

    void update(AboutDTO aboutDTO);

    void delete(Long id);

}
