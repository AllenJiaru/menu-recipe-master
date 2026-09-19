package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.OperationLog;
import com.shiyu.mapper.OperationLogMapper;
import com.shiyu.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public void log(String username, String action, String target, Long targetId, String detail, String ip) {
        OperationLog log = new OperationLog();
        log.setUsername(username);
        log.setAction(action);
        log.setTarget(target);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    @Override
    public PageResponse<OperationLog> getLogs(int page, int size, String keyword) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), OperationLog::getAction, keyword)
                .or()
                .like(StringUtils.hasText(keyword), OperationLog::getTarget, keyword)
                .or()
                .like(StringUtils.hasText(keyword), OperationLog::getUsername, keyword);
        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> pageResult = operationLogMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }
}
