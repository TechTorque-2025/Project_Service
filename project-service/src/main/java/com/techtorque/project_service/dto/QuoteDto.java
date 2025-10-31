package com.techtorque.project_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteDto {

    @NotNull(message = "Quote amount is required")
    @Min(value = 0, message = "Quote amount must be positive")
    private BigDecimal quoteAmount;

    private String notes;
}
