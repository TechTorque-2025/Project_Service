package com.techtorque.project_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
@Tag(name = "Custom Projects (Modifications)", description = "Endpoints for managing custom vehicle modifications.")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

  // @Autowired
  // private ProjectService projectService;

  @Operation(summary = "Request a new modification project (customer only)")
  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> requestModification(
          /* @RequestBody ProjectRequestDto dto, */
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to projectService.requestNewProject(...);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "List projects for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> listCustomerProjects(@RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to projectService.getProjectsForCustomer(customerId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get details for a specific project")
  @GetMapping("/{projectId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
  public ResponseEntity<?> getProjectDetails(@PathVariable String projectId) {
    // TODO: Delegate to projectService, ensuring access rights
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Submit a quote for a project (employee/admin only)")
  @PutMapping("/{projectId}/quote")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<?> submitQuote(@PathVariable String projectId /*, @RequestBody QuoteDto dto */) {
    // TODO: Delegate to projectService.submitQuoteForProject(...);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Accept a quote for a project (customer only)")
  @PostMapping("/{projectId}/accept")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> acceptQuote(
          @PathVariable String projectId,
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to projectService.acceptQuote(projectId, customerId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Reject a quote for a project (customer only)")
  @PostMapping("/{projectId}/reject")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> rejectQuote(
          @PathVariable String projectId,
          // @RequestBody RejectionDto dto,
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to projectService.rejectQuote(projectId, dto, customerId);
    return ResponseEntity.ok().build();
  }
}