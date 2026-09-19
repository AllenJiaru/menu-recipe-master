package com.shiyu.service;

import com.shiyu.dto.request.SupplierRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Supplier;

public interface SupplierService {
    PageResponse<Supplier> getSuppliers(int page, int size, String keyword, String category);
    Supplier getSupplierById(Long id);
    void createSupplier(SupplierRequest request);
    void updateSupplier(Long id, SupplierRequest request);
    void deleteSupplier(Long id);
}
