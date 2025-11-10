package com.techtorque.project_service.controller;

import com.techtorque.project_service.dto.request.*;
import com.techtorque.project_service.dto.response.*;
import com.techtorque.project_service.entity.Project;
import com.techtorque.project_service.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
@Tag(name = "Custom Projects (Modifications)", description = "Endpoints for managing custom vehicle modifications.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectService projectService;

  @Operation(summary = "Request a new modification project (customer only)")
  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> requestModification(
          @Valid @RequestBody ProjectRequestDto dto,
          @RequestHeader("X-User-Subject") String customerId) {

    Project project = projectService.requestNewProject(dto, customerId);
    ProjectResponseDto response = mapToResponseDto(project);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Project request submitted successfully", response));
  }

  @Operation(summary = "List projects for the current customer")
  @GetMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'EMPLOYEE')")
  public ResponseEntity<ApiResponse> listCustomerProjects(
          @RequestHeader("X-User-Subject") String userId,
          @RequestHeader("X-User-Roles") String roles) {
    
    List<Project> projects;
    
    // Admin and Employee can see all projects
    if (roles.contains("ADMIN") || roles.contains("EMPLOYEE")) {
      projects = projectService.getAllProjects();
    } else {
      // Customer sees only their own projects
      projects = projectService.getProjectsForCustomer(userId);
    }
    
    List<ProjectResponseDto> response = projects.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", response));
  }

  @Operation(summary = "Get details for a specific project")
  @GetMapping("/{projectId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE', 'ADMIN')")
  public ResponseEntity<ApiResponse> getProjectDetails(
          @PathVariable String projectId,
          @RequestHeader("X-User-Subject") String userId,
          @RequestHeader("X-User-Roles") String userRoles) {

    Project project = projectService.getProjectDetails(projectId, userId, userRoles)
            .orElseThrow(() -> new RuntimeException("Project not found or access denied"));

    ProjectResponseDto response = mapToResponseDto(project);
    return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", response));
  }

  @Operation(summary = "Submit a quote for a project (employee/admin only)")
  @PutMapping("/{projectId}/quote")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<ApiResponse> submitQuote(
          @PathVariable String projectId,
          @Valid @RequestBody QuoteDto dto) {

    Project project = projectService.submitQuoteForProject(projectId, dto);
    ProjectResponseDto response = mapToResponseDto(project);

    return ResponseEntity.ok(ApiResponse.success("Quote submitted successfully", response));
  }

  @Operation(summary = "Accept a quote for a project (customer only)")
  @PostMapping("/{projectId}/accept")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> acceptQuote(
          @PathVariable String projectId,
          @RequestHeader("X-User-Subject") String customerId) {

    Project project = projectService.acceptQuote(projectId, customerId);
    ProjectResponseDto response = mapToResponseDto(project);

    return ResponseEntity.ok(ApiResponse.success("Quote accepted successfully", response));
  }

  @Operation(summary = "Reject a quote for a project (customer only)")
  @PostMapping("/{projectId}/reject")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> rejectQuote(
          @PathVariable String projectId,
          @Valid @RequestBody RejectionDto dto,
          @RequestHeader("X-User-Subject") String customerId) {

    Project project = projectService.rejectQuote(projectId, dto, customerId);
    ProjectResponseDto response = mapToResponseDto(project);

    return ResponseEntity.ok(ApiResponse.success("Quote rejected successfully", response));
  }

  @Operation(summary = "Update project progress (employee/admin only)")
  @PutMapping("/{projectId}/progress")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<ApiResponse> updateProgress(
          @PathVariable String projectId,
          @Valid @RequestBody ProgressUpdateDto dto) {

    Project project = projectService.updateProgress(projectId, dto);
    ProjectResponseDto response = mapToResponseDto(project);

    return ResponseEntity.ok(ApiResponse.success("Progress updated successfully", response));
  }

  @Operation(summary = "List all projects (admin/employee only)")
  @GetMapping("/all")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<ApiResponse> listAllProjects() {
    List<Project> projects = projectService.getAllProjects();
    List<ProjectResponseDto> response = projects.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success("All projects retrieved successfully", response));
  }

  @Operation(summary = "Approve a custom project request (admin only)")
  @PostMapping("/{projectId}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse> approveProject(
          @PathVariable String projectId,
          @RequestHeader("X-User-Subject") String adminId) {

    Project project = projectService.approveProject(projectId, adminId);
    ProjectResponseDto response = mapToResponseDto(project);

    return ResponseEntity.ok(ApiResponse.success("Project approved successfully", response));
  }

  @Operation(summary = "Reject a custom project request (admin only)")
  @PostMapping("/{projectId}/admin/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse> rejectProject(
          @PathVariable String projectId,
          @RequestParam(required = false) String reason,
          @RequestHeader("X-User-Subject") String adminId) {

    Project project = projectService.rejectProject(projectId, reason, adminId);
    ProjectResponseDto response = mapToResponseDto(project);

    return ResponseEntity.ok(ApiResponse.success("Project rejected successfully", response));
  }

  // Helper method to map Entity to DTO
  private ProjectResponseDto mapToResponseDto(Project project) {
    return ProjectResponseDto.builder()
            .id(project.getId())
            .customerId(project.getCustomerId())
            .vehicleId(project.getVehicleId())
            .projectType(project.getProjectType())
            .description(project.getDescription())
            .desiredCompletionDate(project.getDesiredCompletionDate())
            .budget(project.getBudget())
            .status(project.getStatus())
            .progress(project.getProgress())
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .build();
  }
}