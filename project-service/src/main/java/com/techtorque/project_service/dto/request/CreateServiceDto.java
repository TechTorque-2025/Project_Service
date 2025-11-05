package com.techtorque.project_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceDto {
    @NotBlank(message = "Appointment ID is required")
    private String appointmentId;

    @NotNull(message = "Estimated hours is required")
    private Double estimatedHours;

    private Set<String> assignedEmployeeIds;

    private String customerId;
}
