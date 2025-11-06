package com.techtorque.project_service.controller;

import com.techtorque.project_service.dto.request.*;
import com.techtorque.project_service.dto.response.*;
import com.techtorque.project_service.entity.StandardService;
import com.techtorque.project_service.service.StandardServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
@Tag(name = "Standard Services", description = "Endpoints for managing appointment-based services.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ServiceController {

  private final StandardServiceService standardServiceService;

  @Operation(summary = "Create a service from an appointment (employee only)")
  @PostMapping
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<ApiResponse> createService(
          @Valid @RequestBody CreateServiceDto dto,
          @RequestHeader("X-User-Subject") String employeeId) {
    StandardService service = standardServiceService.createServiceFromAppointment(dto, employeeId);
    ServiceResponseDto response = mapToServiceResponseDto(service);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Service created successfully", response));
  }

  @Operation(summary = "List services for the current customer")
  @GetMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'EMPLOYEE')")
  public ResponseEntity<ApiResponse> listCustomerServices(
          @RequestHeader("X-User-Subject") String userId,
          @RequestHeader("X-User-Roles") String roles,
          @RequestParam(required = false) String status) {
    
    List<StandardService> services;
    
    // Admin and Employee can see all services
    if (roles.contains("ADMIN") || roles.contains("EMPLOYEE")) {
      services = standardServiceService.getAllServices();
      // Apply status filter if provided
      if (status != null && !status.isEmpty()) {
        try {
          com.techtorque.project_service.entity.ServiceStatus statusEnum = 
              com.techtorque.project_service.entity.ServiceStatus.valueOf(status.toUpperCase());
          services = services.stream()
              .filter(s -> s.getStatus() == statusEnum)
              .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
          // Invalid status, ignore filter
        }
      }
    } else {
      // Customer sees only their own services
      services = standardServiceService.getServicesForCustomer(userId, status);
    }
    
    List<ServiceResponseDto> response = services.stream()
            .map(this::mapToServiceResponseDto)
            .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success("Services retrieved successfully", response));
  }

  @Operation(summary = "Get details for a specific service")
  @GetMapping("/{serviceId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<ApiResponse> getServiceDetails(
          @PathVariable String serviceId,
          @RequestHeader("X-User-Subject") String userId,
          @RequestHeader("X-User-Roles") String userRole) {
    return standardServiceService.getServiceDetails(serviceId, userId, userRole)
            .map(service -> ResponseEntity.ok(
                    ApiResponse.success("Service retrieved successfully", mapToServiceResponseDto(service))))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Service not found or access denied")));
  }

  @Operation(summary = "Update service status, notes, or completion estimate (employee only)")
  @PatchMapping("/{serviceId}")
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<ApiResponse> updateService(
          @PathVariable String serviceId,
          @Valid @RequestBody ServiceUpdateDto dto,
          @RequestHeader("X-User-Subject") String employeeId) {
    StandardService service = standardServiceService.updateService(serviceId, dto, employeeId);
    ServiceResponseDto response = mapToServiceResponseDto(service);
    return ResponseEntity.ok(ApiResponse.success("Service updated successfully", response));
  }

  @Operation(summary = "Mark a service as complete and generate an invoice (employee only)")
  @PostMapping("/{serviceId}/complete")
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<ApiResponse> markServiceComplete(
          @PathVariable String serviceId,
          @Valid @RequestBody CompletionDto dto,
          @RequestHeader("X-User-Subject") String employeeId) {
    InvoiceDto invoice = standardServiceService.completeService(serviceId, dto, employeeId);
    return ResponseEntity.ok(ApiResponse.success("Service completed successfully", invoice));
  }

  @Operation(summary = "Get invoice for a service (customer only)")
  @GetMapping("/{serviceId}/invoice")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> getServiceInvoice(
          @PathVariable String serviceId,
          @RequestHeader("X-User-Subject") String customerId) {
    InvoiceDto invoice = standardServiceService.getServiceInvoice(serviceId, customerId);
    return ResponseEntity.ok(ApiResponse.success("Invoice retrieved successfully", invoice));
  }

  @Operation(summary = "Add a work note to a service (employee only)")
  @PostMapping("/{serviceId}/notes")
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<ApiResponse> addServiceNote(
          @PathVariable String serviceId,
          @Valid @RequestBody NoteDto dto,
          @RequestHeader("X-User-Subject") String employeeId) {
    NoteResponseDto note = standardServiceService.addServiceNote(serviceId, dto, employeeId);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Note added successfully", note));
  }

  @Operation(summary = "Get all notes for a service")
  @GetMapping("/{serviceId}/notes")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<ApiResponse> getServiceNotes(
          @PathVariable String serviceId,
          @RequestHeader("X-User-Subject") String userId,
          @RequestHeader("X-User-Roles") String userRole) {
    List<NoteResponseDto> notes = standardServiceService.getServiceNotes(serviceId, userId, userRole);
    return ResponseEntity.ok(ApiResponse.success("Notes retrieved successfully", notes));
  }

  @Operation(summary = "Upload progress photos for a service (employee only)")
  @PostMapping("/{serviceId}/photos")
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<ApiResponse> uploadProgressPhotos(
          @PathVariable String serviceId,
          @RequestParam("files") MultipartFile[] files,
          @RequestHeader("X-User-Subject") String employeeId) {
    List<PhotoDto> photos = standardServiceService.uploadPhotos(serviceId, files, employeeId);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Photos uploaded successfully", photos));
  }

  @Operation(summary = "Get all progress photos for a service")
  @GetMapping("/{serviceId}/photos")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<ApiResponse> getProgressPhotos(@PathVariable String serviceId) {
    List<PhotoDto> photos = standardServiceService.getPhotos(serviceId);
    return ResponseEntity.ok(ApiResponse.success("Photos retrieved successfully", photos));
  }

  // Helper method to map Entity to DTO
  private ServiceResponseDto mapToServiceResponseDto(StandardService service) {
    return ServiceResponseDto.builder()
            .id(service.getId())
            .appointmentId(service.getAppointmentId())
            .customerId(service.getCustomerId())
            .assignedEmployeeIds(service.getAssignedEmployeeIds())
            .status(service.getStatus())
            .progress(service.getProgress())
            .hoursLogged(service.getHoursLogged())
            .estimatedCompletion(service.getEstimatedCompletion())
            .createdAt(service.getCreatedAt())
            .updatedAt(service.getUpdatedAt())
            .build();
  }
}