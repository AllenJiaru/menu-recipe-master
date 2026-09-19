package com.shiyu.service;

import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.GalleryImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GalleryService {
    PageResponse<GalleryImage> getImages(Integer page, Integer size, Long coupleId);
    GalleryImage getImageById(Long id);
    GalleryImage uploadImage(MultipartFile file, String description, Long coupleId, Long orderId);
    GalleryImage updateImage(Long id, String description);
    void deleteImage(Long id);
    void batchDelete(List<Long> ids);
}
