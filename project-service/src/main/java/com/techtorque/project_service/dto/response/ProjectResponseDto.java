package com.techtorque.project_service.dto.response;

import com.techtorque.project_service.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {
    private String id;
    private String customerId;
    private String vehicleId;
    private String projectType;
    private String description;
    private String desiredCompletionDate;
    private BigDecimal budget;
    private ProjectStatus status;
    private int progress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
