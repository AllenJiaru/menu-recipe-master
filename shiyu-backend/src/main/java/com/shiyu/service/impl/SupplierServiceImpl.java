package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.SupplierRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Supplier;
import com.shiyu.mapper.SupplierMapper;
import com.shiyu.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    public PageResponse<Supplier> getSuppliers(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), Supplier::getName, keyword);
        wrapper.eq(StringUtils.hasText(category), Supplier::getCategory, category);
        wrapper.orderByDesc(Supplier::getCreateTime);

        Page<Supplier> pageResult = supplierMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public Supplier getSupplierById(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException("Supplier not found");
        }
        return supplier;
    }

    @Override
    public void createSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setContact(request.getContact());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setCategory(request.getCategory());
        supplier.setRating(request.getRating());
        supplier.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        supplier.setRemark(request.getRemark());
        supplier.setCreateTime(LocalDateTime.now());
        supplier.setUpdateTime(LocalDateTime.now());
        supplierMapper.insert(supplier);
    }

    @Override
    public void updateSupplier(Long id, SupplierRequest request) {
        Supplier existing = supplierMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Supplier not found");
        }

        existing.setName(request.getName());
        existing.setContact(request.getContact());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setCategory(request.getCategory());
        existing.setRating(request.getRating());
        existing.setRemark(request.getRemark());
        existing.setUpdateTime(LocalDateTime.now());
        supplierMapper.updateById(existing);
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier existing = supplierMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Supplier not found");
        }
        supplierMapper.deleteById(id);
    }
}
