package com.techtorque.project_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponseDto {
    private String id;
    private String note;
    private String employeeId;
    private boolean isCustomerVisible;
    private LocalDateTime createdAt;
}
