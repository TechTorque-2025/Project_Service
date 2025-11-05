package com.techtorque.project_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_photos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String serviceId;

    @Column(nullable = false)
    private String photoUrl;

    private String description;

    @Column(nullable = false)
    private String uploadedBy;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
