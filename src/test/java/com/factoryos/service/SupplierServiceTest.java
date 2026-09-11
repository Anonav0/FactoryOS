package com.factoryos.service;

import com.factoryos.dto.CreateSupplierRequest;
import com.factoryos.dto.SupplierResponse;
import com.factoryos.dto.UpdateSupplierRequest;
import com.factoryos.entity.Supplier;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.mapper.SupplierMapper;
import com.factoryos.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Spy
    private SupplierMapper supplierMapper = new SupplierMapper();

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testSupplier = Supplier.builder()
                .id(1L)
                .name("ABC Industrial Supplies")
                .contactPerson("Rahul Sen")
                .email("contact@abcindustrial.example")
                .phone("+91-9876543210")
                .address("Industrial Area, Kolkata")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void createSupplier_success() {
        CreateSupplierRequest request = new CreateSupplierRequest(
                "ABC Industrial Supplies",
                "Rahul Sen",
                "contact@abcindustrial.example",
                "+91-9876543210",
                "Industrial Area, Kolkata",
                true
        );

        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        SupplierResponse response = supplierService.createSupplier(request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("ABC Industrial Supplies");
        assertThat(response.email()).isEqualTo("contact@abcindustrial.example");
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void getSupplierById_success() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        SupplierResponse response = supplierService.getSupplierById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("ABC Industrial Supplies");
    }

    @Test
    void getSupplierById_notFound_throwsResourceNotFoundException() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getSupplierById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateSupplier_success() {
        UpdateSupplierRequest request = new UpdateSupplierRequest(
                "ABC Enterprises",
                "Amit Sen",
                "amit@abc.example",
                "+91-1122334455",
                "New Industrial Zone",
                true
        );

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        SupplierResponse response = supplierService.updateSupplier(1L, request);

        assertThat(response).isNotNull();
        assertThat(testSupplier.getName()).isEqualTo("ABC Enterprises");
        assertThat(testSupplier.getContactPerson()).isEqualTo("Amit Sen");
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    void deactivateSupplier_success_setsActiveFalse() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        supplierService.deactivateSupplier(1L);

        assertThat(testSupplier.getActive()).isFalse();
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    void getActiveSuppliers_success() {
        when(supplierRepository.findByActiveTrue()).thenReturn(List.of(testSupplier));

        List<SupplierResponse> activeSuppliers = supplierService.getActiveSuppliers();

        assertThat(activeSuppliers).hasSize(1);
        assertThat(activeSuppliers.get(0).active()).isTrue();
    }
}

