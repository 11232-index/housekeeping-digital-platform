package com.smart.service;

import com.smart.entity.Feed;
import java.util.List;

public interface FeedService {
    /** 新增feed（被updates/service-intro的save调用） */
    void save(Feed feed);
    /** 删除updates关联的feed */
    void deleteByUpdatesId(Long updatesId);
    /** 删除service_intro关联的feed */
    void deleteByServiceIntroId(Long serviceIntroId);
    /** 联表查询feed列表 */
    List<Feed> list(int limit);
}
