package com.shiyu.service;

import com.shiyu.dto.request.NoticeRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Notice;
import java.util.List;

public interface NoticeService {
    PageResponse<Notice> getNotices(int page, int size, String type, Integer status);
    Notice getNoticeById(Long id);
    void createNotice(NoticeRequest request, String authorName, Long authorId);
    void updateNotice(Long id, NoticeRequest request);
    void deleteNotice(Long id);
    void publishNotice(Long id);
    List<Notice> getPublishedNotices();
}
