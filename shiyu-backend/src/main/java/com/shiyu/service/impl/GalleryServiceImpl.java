package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.GalleryImage;
import com.shiyu.mapper.GalleryImageMapper;
import com.shiyu.service.GalleryService;
import com.shiyu.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class GalleryServiceImpl implements GalleryService {

    @Autowired
    private GalleryImageMapper galleryImageMapper;

    @Autowired
    private FileUtil fileUtil;

    @Override
    public PageResponse<GalleryImage> getImages(Integer page, Integer size, Long coupleId) {
        page = page == null ? 1 : page;
        size = size == null ? 10 : size;

        LambdaQueryWrapper<GalleryImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(coupleId != null, GalleryImage::getCoupleId, coupleId);
        wrapper.orderByDesc(GalleryImage::getCreateTime);

        Page<GalleryImage> pageResult = galleryImageMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public GalleryImage getImageById(Long id) {
        GalleryImage image = galleryImageMapper.selectById(id);
        if (image == null) {
            throw new BusinessException("Image not found");
        }
        return image;
    }

    @Override
    public GalleryImage uploadImage(MultipartFile file, String description, Long coupleId, Long orderId) {
        try {
            String imageUrl = fileUtil.uploadFile(file, "gallery");
            String thumbnailUrl = fileUtil.generateThumbnail(imageUrl);

            GalleryImage image = new GalleryImage();
            image.setCoupleId(coupleId);
            image.setImageUrl(imageUrl);
            image.setThumbnailUrl(thumbnailUrl);
            image.setDescription(description);
            image.setOrderId(orderId);
            image.setSyncId(UUID.randomUUID().toString());
            galleryImageMapper.insert(image);
            return image;
        } catch (Exception e) {
            throw new BusinessException("Image upload failed: " + e.getMessage());
        }
    }

    @Override
    public GalleryImage updateImage(Long id, String description) {
        GalleryImage image = getImageById(id);
        image.setDescription(description);
        galleryImageMapper.updateById(image);
        return image;
    }

    @Override
    public void deleteImage(Long id) {
        GalleryImage image = getImageById(id);
        fileUtil.deleteFile(image.getImageUrl());
        galleryImageMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            deleteImage(id);
        }
    }
}
