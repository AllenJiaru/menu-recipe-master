package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.GalleryImage;
import com.shiyu.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    @OperationLog(action = "QUERY", target = "相册")
    @GetMapping
    public ApiResponse<PageResponse<GalleryImage>> getImages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long coupleId) {
        return ApiResponse.success(galleryService.getImages(page, size, coupleId));
    }

    @OperationLog(action = "QUERY", target = "相册")
    @GetMapping("/{id}")
    public ApiResponse<GalleryImage> getImageById(@PathVariable Long id) {
        return ApiResponse.success(galleryService.getImageById(id));
    }

    @OperationLog(action = "CREATE", target = "相册")
    @PostMapping
    public ApiResponse<GalleryImage> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long coupleId,
            @RequestParam(required = false) Long orderId) {
        return ApiResponse.success(galleryService.uploadImage(file, description, coupleId, orderId));
    }

    @OperationLog(action = "UPDATE", target = "相册")
    @PutMapping("/{id}")
    public ApiResponse<GalleryImage> updateImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ApiResponse.success(galleryService.updateImage(id, body.get("description")));
    }

    @OperationLog(action = "DELETE", target = "相册")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteImage(@PathVariable Long id) {
        galleryService.deleteImage(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "BATCH_DELETE", target = "相册")
    @PostMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> ids) {
        galleryService.batchDelete(ids);
        return ApiResponse.success();
    }
}
