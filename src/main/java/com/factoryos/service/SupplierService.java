package com.factoryos.service;

import com.factoryos.dto.CreateSupplierRequest;
import com.factoryos.dto.SupplierResponse;
import com.factoryos.dto.UpdateSupplierRequest;

import java.util.List;

public interface SupplierService {

    SupplierResponse createSupplier(CreateSupplierRequest request);

    SupplierResponse getSupplierById(Long id);

    List<SupplierResponse> getAllSuppliers();

    List<SupplierResponse> getActiveSuppliers();

    SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request);

    void deactivateSupplier(Long id);
}

