package com.smart.service;

import com.smart.dto.HotPicksDTO;
import com.smart.dto.HotPicksPageQueryDTO;
import com.smart.entity.HotPicks;
import com.smart.result.PageResult;

public interface HotPicksService {

    void save(HotPicksDTO hotPicksDTO);

    PageResult pageQuery(HotPicksPageQueryDTO dto);

    HotPicks getById(Long id);

    void update(HotPicksDTO hotPicksDTO);

    void startOrStop(Integer status, Long id);

    void delete(Long id);

}
