package com.techtorque.project_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "standard_services")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardService {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false, unique = true)
  private String appointmentId; // Link to the appointment

  private String customerId;

  @ElementCollection(fetch = FetchType.EAGER) // Store a collection of simple strings
  private Set<String> assignedEmployeeIds;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ServiceStatus status;

  private int progress; // Percentage, e.g., 0-100

  private double hoursLogged;

  private LocalDateTime estimatedCompletion;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}