package com.shiyu.service;

import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.OperationLog;

public interface OperationLogService {
    void log(String username, String action, String target, Long targetId, String detail, String ip);
    PageResponse<OperationLog> getLogs(int page, int size, String keyword);
}
