package com.smart.service;

import com.smart.entity.ServiceIntroSearchHistory;
import java.util.List;

public interface ServiceIntroSearchHistoryService {
    void save(String keyword, Long userId);
    List<ServiceIntroSearchHistory> listByUserId(Long userId);
    void clean(Long userId);
}
