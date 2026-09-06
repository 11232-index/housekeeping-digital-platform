package com.smart.service;

import com.smart.entity.HousekeeperSearchHistory;
import java.util.List;

public interface HousekeeperSearchHistoryService {
    void save(String keyword, Long userId);
    List<HousekeeperSearchHistory> listByUserId(Long userId);
    void clean(Long userId);
}
