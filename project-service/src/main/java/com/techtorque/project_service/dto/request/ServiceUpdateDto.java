package com.techtorque.project_service.dto.request;

import com.techtorque.project_service.entity.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateDto {
    private ServiceStatus status;
    private String notes;
    private LocalDateTime estimatedCompletion;
    private Integer progress;
}
