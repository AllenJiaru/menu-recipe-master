package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.GalleryImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GalleryImageMapper extends BaseMapper<GalleryImage> {

    @Select("SELECT * FROM gallery_image WHERE update_time > #{lastSyncTime} AND deleted = 0")
    List<GalleryImage> findModifiedAfter(@Param("lastSyncTime") LocalDateTime lastSyncTime);
}
