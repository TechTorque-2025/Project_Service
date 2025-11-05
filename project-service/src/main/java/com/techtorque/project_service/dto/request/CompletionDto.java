package com.techtorque.project_service.dto.request;

import com.techtorque.project_service.dto.response.InvoiceItemDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionDto {
    @NotBlank(message = "Final notes are required")
    @Size(min = 10, max = 2000, message = "Final notes must be between 10 and 2000 characters")
    private String finalNotes;

    @NotNull(message = "Actual cost is required")
    private BigDecimal actualCost;

    private List<InvoiceItemDto> additionalCharges;
}
