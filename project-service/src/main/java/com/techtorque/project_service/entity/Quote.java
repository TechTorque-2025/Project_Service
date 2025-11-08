package com.techtorque.project_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String projectId;

    @Column(nullable = false)
    private BigDecimal laborCost;

    @Column(nullable = false)
    private BigDecimal partsCost;

    @Column(nullable = false)
    private BigDecimal totalCost;

    @Column(nullable = false)
    private int estimatedDays;

    @Lob
    private String breakdown;

    @Column(nullable = false)
    private String submittedBy;

    @CreationTimestamp
    private LocalDateTime submittedAt;
}
