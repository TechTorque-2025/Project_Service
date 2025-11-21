package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.Invoice;
import com.techtorque.project_service.entity.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();

        testInvoice = Invoice.builder()
                .invoiceNumber("INV-20251121001")
                .serviceId("service123")
                .customerId("customer123")
                .items(new ArrayList<>())
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("1150.00"))
                .status(InvoiceStatus.PENDING)
                .build();
    }

    @Test
    void testSaveInvoice() {
        Invoice saved = invoiceRepository.save(testInvoice);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getInvoiceNumber()).isEqualTo("INV-20251121001");
        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    }

    @Test
    void testFindById() {
        invoiceRepository.save(testInvoice);

        Optional<Invoice> found = invoiceRepository.findById(testInvoice.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getServiceId()).isEqualTo("service123");
    }

    @Test
    void testFindByServiceId() {
        invoiceRepository.save(testInvoice);

        Optional<Invoice> found = invoiceRepository.findByServiceId("service123");

        assertThat(found).isPresent();
        assertThat(found.get().getInvoiceNumber()).isEqualTo("INV-20251121001");
    }

    @Test
    void testFindByInvoiceNumber() {
        invoiceRepository.save(testInvoice);

        Optional<Invoice> found = invoiceRepository.findByInvoiceNumber("INV-20251121001");

        assertThat(found).isPresent();
        assertThat(found.get().getServiceId()).isEqualTo("service123");
    }

    @Test
    void testUpdateInvoiceStatus() {
        invoiceRepository.save(testInvoice);

        testInvoice.setStatus(InvoiceStatus.PAID);
        Invoice updated = invoiceRepository.save(testInvoice);

        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void testDeleteInvoice() {
        invoiceRepository.save(testInvoice);
        String invoiceId = testInvoice.getId();

        invoiceRepository.deleteById(invoiceId);

        Optional<Invoice> deleted = invoiceRepository.findById(invoiceId);
        assertThat(deleted).isEmpty();
    }
}
