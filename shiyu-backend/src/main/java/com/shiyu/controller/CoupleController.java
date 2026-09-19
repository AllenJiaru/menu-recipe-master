package com.shiyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.ApiResponse;
import com.shiyu.entity.CoupleConfig;
import com.shiyu.mapper.CoupleConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/couples")
public class CoupleController {

    @Autowired
    private CoupleConfigMapper coupleConfigMapper;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CoupleConfig> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CoupleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CoupleConfig::getCreateTime);
        Page<CoupleConfig> result = coupleConfigMapper.selectPage(pageParam, wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return ApiResponse.success(data);
    }

    @GetMapping("/{id}")
    public ApiResponse<CoupleConfig> getById(@PathVariable Long id) {
        CoupleConfig couple = coupleConfigMapper.selectById(id);
        if (couple == null) {
            return ApiResponse.error(404, "Couple not found");
        }
        return ApiResponse.success(couple);
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody CoupleConfig couple) {
        couple.setStatus(1);
        couple.setDeleted(0);
        coupleConfigMapper.insert(couple);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CoupleConfig couple) {
        couple.setId(id);
        coupleConfigMapper.updateById(couple);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        coupleConfigMapper.deleteById(id);
        return ApiResponse.success();
    }
}
