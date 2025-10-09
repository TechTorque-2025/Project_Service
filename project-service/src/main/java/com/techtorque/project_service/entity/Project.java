package com.techtorque.project_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String customerId;

  @Column(nullable = false)
  private String vehicleId;

  @Lob
  @Column(nullable = false)
  private String description;

  private BigDecimal budget; // Use BigDecimal for currency

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectStatus status;

  private int progress;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}