package com.techtorque.project_service.controller;

import com.techtorque.project_service.dto.ApiResponse;
import com.techtorque.project_service.entity.StandardService;
import com.techtorque.project_service.service.StandardServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/services")
@Tag(name = "Standard Services", description = "Endpoints for managing appointment-based services.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ServiceController {

  private final StandardServiceService standardServiceService;

  @Operation(summary = "List services for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> listCustomerServices(
          @RequestHeader("X-User-Subject") String customerId,
          @RequestParam(required = false) String status) {
    List<StandardService> services = standardServiceService.getServicesForCustomer(customerId, status);
    return ResponseEntity.ok(ApiResponse.success("Services retrieved successfully", services));
  }

  @Operation(summary = "Get details for a specific service")
  @GetMapping("/{serviceId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<ApiResponse> getServiceDetails(
          @PathVariable String serviceId,
          @RequestHeader("X-User-Subject") String userId,
          @RequestHeader("X-User-Roles") String userRole) {
    return standardServiceService.getServiceDetails(serviceId, userId, userRole)
            .map(service -> ResponseEntity.ok(ApiResponse.success("Service retrieved successfully", service)))
            .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Update service status, notes, or completion estimate (employee only)")
  @PatchMapping("/{serviceId}")
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<?> updateService(
          @PathVariable String serviceId,
          /* @RequestBody ServiceUpdateDto dto */
          @RequestHeader("X-User-Subject") String employeeId) {
    // TODO: Delegate to serviceLayer.updateService(...);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Mark a service as complete and generate an invoice (employee only)")
  @PostMapping("/{serviceId}/complete")
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<?> markServiceComplete(@PathVariable String serviceId /*, @RequestBody CompletionDto dto */) {
    // TODO: Delegate to serviceLayer.completeService(...); This may involve an inter-service call to the Billing Service.
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Add a work note to a service (employee only)")
  @PostMapping("/{serviceId}/notes")
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<?> addServiceNote(@PathVariable String serviceId /*, @RequestBody NoteDto dto */) {
    // TODO: Delegate to serviceLayer.addNote(...);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get all notes for a service")
  @GetMapping("/{serviceId}/notes")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<?> getServiceNotes(@PathVariable String serviceId) {
    // TODO: Delegate to serviceLayer.getNotes(...); The service should filter notes based on the user's role.
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Upload progress photos for a service (employee only)")
  @PostMapping("/{serviceId}/photos")
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<?> uploadProgressPhotos(
          @PathVariable String serviceId,
          @RequestParam("files") MultipartFile[] files) {
    // TODO: Delegate to serviceLayer.uploadPhotos(...);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get all progress photos for a service")
  @GetMapping("/{serviceId}/photos")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<?> getProgressPhotos(@PathVariable String serviceId) {
    // TODO: Delegate to serviceLayer.getPhotos(...);
    return ResponseEntity.ok().build();
  }
}