package com.techtorque.project_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceNoteDto {

    @NotBlank(message = "Note cannot be empty")
    private String note;

    @NotNull(message = "isInternal flag is required")
    private Boolean isInternal;
}
