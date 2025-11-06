package com.techtorque.project_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String serviceId;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false, length = 1000)
    private String note;

    @Column(nullable = false)
    private Boolean isInternal; // Internal notes are only visible to employees

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
