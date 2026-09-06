package com.smart.service;

import com.smart.entity.UpdatesSearchHistory;

import java.util.List;

public interface UpdatesSearchHistoryService {

    void save(String keyword, Long userId);

    List<UpdatesSearchHistory> listByUserId(Long userId);

    void clean(Long userId);

}
