package com.shiyu.service;

import com.shiyu.dto.request.SyncRequest;
import java.util.Map;

public interface SyncService {
    Map<String, Object> pull(SyncRequest request);
    Map<String, Object> push(SyncRequest request);
    Map<String, Object> getSyncLogs(Integer page, Integer size);
}
