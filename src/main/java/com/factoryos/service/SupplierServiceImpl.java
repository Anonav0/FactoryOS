package com.factoryos.service;

import com.factoryos.dto.CreateSupplierRequest;
import com.factoryos.dto.SupplierResponse;
import com.factoryos.dto.UpdateSupplierRequest;
import com.factoryos.entity.Supplier;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.mapper.SupplierMapper;
import com.factoryos.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        Supplier supplier = supplierMapper.toEntity(request);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(savedSupplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = findSupplierOrThrow(id);
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getActiveSuppliers() {
        return supplierRepository.findByActiveTrue().stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
        Supplier supplier = findSupplierOrThrow(id);

        supplier.setName(request.name().trim());
        supplier.setContactPerson(request.contactPerson() != null ? request.contactPerson().trim() : null);
        supplier.setEmail(request.email() != null ? request.email().trim().toLowerCase() : null);
        supplier.setPhone(request.phone() != null ? request.phone().trim() : null);
        supplier.setAddress(request.address() != null ? request.address().trim() : null);
        if (request.active() != null) {
            supplier.setActive(request.active());
        }

        Supplier updatedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(updatedSupplier);
    }

    @Override
    @Transactional
    public void deactivateSupplier(Long id) {
        Supplier supplier = findSupplierOrThrow(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    private Supplier findSupplierOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }
}

