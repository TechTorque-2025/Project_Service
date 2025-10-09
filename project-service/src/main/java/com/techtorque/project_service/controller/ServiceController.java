package com.techtorque.project_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services")
@Tag(name = "Standard Services", description = "Endpoints for managing appointment-based services.")
public class ServiceController {

  @Operation(summary = "Create a new service from an appointment (employee only)")
  @PostMapping
  @PreAuthorize("hasRole('EMPLOYEE')")
  public ResponseEntity<?> createServiceFromAppointment(/* @RequestBody CreateServiceDto dto */) {
    // TODO: Delegate to a service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get details for a specific service")
  @GetMapping("/{serviceId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<?> getServiceDetails(@PathVariable String serviceId) {
    // TODO: Delegate to a service layer, ensuring customer/employee has access
    return ResponseEntity.ok().build();
  }
}