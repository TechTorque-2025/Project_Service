package com.techtorque.project_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
@Tag(name = "Custom Projects (Modifications)", description = "Endpoints for managing custom vehicle modifications.")
public class ProjectController {

  @Operation(summary = "Request a new modification project (customer only)")
  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> requestModification(
          /* @RequestBody ProjectRequestDto dto, */
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to a project service
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get details for a specific project")
  @GetMapping("/{projectId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<?> getProjectDetails(@PathVariable String projectId) {
    // TODO: Delegate to a project service, ensuring access rights
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Submit a quote for a project (employee/admin only)")
  @PutMapping("/{projectId}/quote")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<?> submitQuote(@PathVariable String projectId /*, @RequestBody QuoteDto dto */) {
    // TODO: Delegate to a project service
    return ResponseEntity.ok().build();
  }
}