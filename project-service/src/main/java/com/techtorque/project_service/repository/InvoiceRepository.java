package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByCustomerId(String customerId);
    Optional<Invoice> findByServiceId(String serviceId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
