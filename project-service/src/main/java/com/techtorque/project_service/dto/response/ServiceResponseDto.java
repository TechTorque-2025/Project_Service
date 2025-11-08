package com.techtorque.project_service.dto.response;

import com.techtorque.project_service.entity.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponseDto {
    private String id;
    private String appointmentId;
    private String customerId;
    private Set<String> assignedEmployeeIds;
    private ServiceStatus status;
    private int progress;
    private double hoursLogged;
    private LocalDateTime estimatedCompletion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
