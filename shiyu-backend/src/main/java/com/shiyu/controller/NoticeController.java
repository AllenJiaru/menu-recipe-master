package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.NoticeRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Notice;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@Tag(name = "公告管理", description = "系统公告 CRUD")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    private String currentUsername() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            return userDetails.getUsername();
        } catch (Exception e) {
            throw new RuntimeException("未登录或登录已过期");
        }
    }

    private Long currentUserId() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            return userDetails.getUserId();
        } catch (Exception e) {
            throw new RuntimeException("未登录或登录已过期");
        }
    }

    @OperationLog(action = "QUERY", target = "公告")
    @Operation(summary = "查询公告列表", description = "分页查询公告，支持类型和状态筛选")
    @GetMapping
    public ApiResponse<PageResponse<Notice>> getNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(noticeService.getNotices(page, size, type, status));
    }

    @OperationLog(action = "QUERY", target = "公告")
    @Operation(summary = "查询公告详情", description = "根据ID查询公告详细信息")
    @GetMapping("/{id}")
    public ApiResponse<Notice> getNoticeById(@PathVariable Long id) {
        return ApiResponse.success(noticeService.getNoticeById(id));
    }

    @OperationLog(action = "CREATE", target = "公告")
    @Operation(summary = "创建公告", description = "发布新公告")
    @PostMapping
    public ApiResponse<Void> createNotice(@Valid @RequestBody NoticeRequest request) {
        noticeService.createNotice(request, currentUsername(), currentUserId());
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "公告")
    @Operation(summary = "更新公告", description = "修改公告内容")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeRequest request) {
        noticeService.updateNotice(id, request);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "公告")
    @Operation(summary = "删除公告", description = "根据ID删除公告")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "公告")
    @Operation(summary = "发布公告", description = "将公告状态改为已发布")
    @PutMapping("/{id}/publish")
    public ApiResponse<Void> publishNotice(@PathVariable Long id) {
        noticeService.publishNotice(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "公告")
    @Operation(summary = "查询已发布公告", description = "获取所有已发布的公告列表")
    @GetMapping("/published")
    public ApiResponse<List<Notice>> getPublishedNotices() {
        return ApiResponse.success(noticeService.getPublishedNotices());
    }
}
