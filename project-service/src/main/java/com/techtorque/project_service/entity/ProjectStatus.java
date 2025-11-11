package com.techtorque.project_service.entity;

public enum ProjectStatus {
  REQUESTED,
  PENDING_ADMIN_REVIEW,  // Custom project awaiting admin approval
  QUOTED,
  APPROVED,
  IN_PROGRESS,
  COMPLETED,
  REJECTED,
  CANCELLED
}