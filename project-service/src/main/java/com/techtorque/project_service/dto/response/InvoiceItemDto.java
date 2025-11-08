package com.techtorque.project_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDto {
    private String id;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
