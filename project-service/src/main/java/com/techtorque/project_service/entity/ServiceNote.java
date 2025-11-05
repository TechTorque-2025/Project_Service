package com.techtorque.project_service.entity;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(nullable = false, length = 2000)
    private String note;

    @Column(nullable = false)
    private boolean isCustomerVisible;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
