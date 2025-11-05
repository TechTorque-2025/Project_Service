package com.techtorque.project_service.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDto {
    @NotBlank(message = "Note is required")
    @Size(min = 5, max = 2000, message = "Note must be between 5 and 2000 characters")
    private String note;

    @Builder.Default
    private boolean isCustomerVisible = false;
}
