package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.OrderRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderRecordMapper extends BaseMapper<OrderRecord> {

    @Select("SELECT * FROM order_record WHERE update_time > #{lastSyncTime} AND deleted = 0")
    List<OrderRecord> findModifiedAfter(@Param("lastSyncTime") LocalDateTime lastSyncTime);
}
