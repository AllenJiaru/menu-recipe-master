package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.NoticeRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Notice;
import com.shiyu.mapper.NoticeMapper;
import com.shiyu.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public PageResponse<Notice> getNotices(int page, int size, String type, Integer status) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(type), Notice::getType, type);
        wrapper.eq(status != null, Notice::getStatus, status);
        wrapper.orderByDesc(Notice::getCreateTime);

        Page<Notice> pageResult = noticeMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public Notice getNoticeById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("Notice not found");
        }
        return notice;
    }

    @Override
    public void createNotice(NoticeRequest request, String authorName, Long authorId) {
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setType(request.getType() != null && !request.getType().isEmpty() ? request.getType() : "notice");
        notice.setPriority(request.getPriority() != null ? request.getPriority() : 1);
        notice.setAuthorId(authorId != null ? authorId : 1L);
        notice.setAuthorName(authorName != null && !authorName.isEmpty() ? authorName : "admin");
        notice.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
    }

    @Override
    public void updateNotice(Long id, NoticeRequest request) {
        Notice existing = noticeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Notice not found");
        }

        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setType(request.getType());
        existing.setPriority(request.getPriority());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(existing);
    }

    @Override
    public void deleteNotice(Long id) {
        Notice existing = noticeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Notice not found");
        }
        noticeMapper.deleteById(id);
    }

    @Override
    public void publishNotice(Long id) {
        Notice existing = noticeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Notice not found");
        }
        existing.setStatus(1);
        existing.setPublishTime(LocalDateTime.now());
        existing.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(existing);
    }

    @Override
    public List<Notice> getPublishedNotices() {
        return noticeMapper.findPublished();
    }
}
