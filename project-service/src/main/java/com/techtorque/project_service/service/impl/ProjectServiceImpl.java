package com.techtorque.project_service.service.impl;

import com.techtorque.project_service.dto.ProgressUpdateDto;
import com.techtorque.project_service.dto.ProjectRequestDto;
import com.techtorque.project_service.dto.QuoteDto;
import com.techtorque.project_service.dto.RejectionDto;
import com.techtorque.project_service.entity.Project;
import com.techtorque.project_service.entity.ProjectStatus;
import com.techtorque.project_service.exception.InvalidProjectOperationException;
import com.techtorque.project_service.exception.ProjectNotFoundException;
import com.techtorque.project_service.repository.ProjectRepository;
import com.techtorque.project_service.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ProjectServiceImpl implements ProjectService {

  private final ProjectRepository projectRepository;

  public ProjectServiceImpl(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Override
  public Project requestNewProject(ProjectRequestDto dto, String customerId) {
    log.info("Creating new project request for customer: {}", customerId);

    // Create new project
    Project newProject = Project.builder()
            .customerId(customerId)
            .vehicleId(dto.getVehicleId())
            .description(dto.getDescription())
            .budget(dto.getBudget())
            .status(ProjectStatus.REQUESTED)
            .progress(0)
            .build();

    Project savedProject = projectRepository.save(newProject);
    log.info("Successfully created project with ID: {} for customer: {}",
             savedProject.getId(), customerId);

    return savedProject;
  }

  @Override
  public List<Project> getProjectsForCustomer(String customerId) {
    log.info("Fetching all projects for customer: {}", customerId);
    return projectRepository.findByCustomerId(customerId);
  }

  @Override
  public Optional<Project> getProjectDetails(String projectId, String userId, String userRole) {
    log.info("Fetching project {} for user: {} with role: {}", projectId, userId, userRole);

    Optional<Project> projectOpt = projectRepository.findById(projectId);

    if (projectOpt.isEmpty()) {
      return Optional.empty();
    }

    Project project = projectOpt.get();

    // Role-based access control
    if (userRole.contains("ADMIN") || userRole.contains("EMPLOYEE")) {
      // Admins and employees can see all projects
      return projectOpt;
    } else if (userRole.contains("CUSTOMER")) {
      // Customers can only see their own projects
      if (project.getCustomerId().equals(userId)) {
        return projectOpt;
      }
    }

    log.warn("User {} with role {} attempted to access project {} without permission",
            userId, userRole, projectId);
    return Optional.empty();
  }

  @Override
  public Project submitQuoteForProject(String projectId, QuoteDto dto) {
    log.info("Submitting quote for project: {}", projectId);

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> {
              log.warn("Project {} not found", projectId);
              return new ProjectNotFoundException("Project not found");
            });

    // Can only quote projects in REQUESTED status
    if (project.getStatus() != ProjectStatus.REQUESTED) {
      throw new InvalidProjectOperationException(
              "Can only quote projects in REQUESTED status. Current status: " + project.getStatus());
    }

    // Update with quote
    project.setBudget(dto.getQuoteAmount());
    project.setStatus(ProjectStatus.QUOTED);

    Project updatedProject = projectRepository.save(project);
    log.info("Successfully submitted quote for project: {}", projectId);

    return updatedProject;
  }

  @Override
  public Project acceptQuote(String projectId, String customerId) {
    log.info("Customer {} accepting quote for project: {}", customerId, projectId);

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> {
              log.warn("Project {} not found", projectId);
              return new ProjectNotFoundException("Project not found");
            });

    // Verify ownership
    if (!project.getCustomerId().equals(customerId)) {
      throw new InvalidProjectOperationException("You don't have permission to accept this quote");
    }

    // Can only accept projects in QUOTED status
    if (project.getStatus() != ProjectStatus.QUOTED) {
      throw new InvalidProjectOperationException(
              "Can only accept projects in QUOTED status. Current status: " + project.getStatus());
    }

    // Update status to approved
    project.setStatus(ProjectStatus.APPROVED);

    Project updatedProject = projectRepository.save(project);
    log.info("Successfully accepted quote for project: {}", projectId);

    // TODO: Inter-service call to Payment Service to generate deposit invoice

    return updatedProject;
  }

  @Override
  public Project rejectQuote(String projectId, RejectionDto dto, String customerId) {
    log.info("Customer {} rejecting quote for project: {}", customerId, projectId);

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> {
              log.warn("Project {} not found", projectId);
              return new ProjectNotFoundException("Project not found");
            });

    // Verify ownership
    if (!project.getCustomerId().equals(customerId)) {
      throw new InvalidProjectOperationException("You don't have permission to reject this quote");
    }

    // Can only reject projects in QUOTED status
    if (project.getStatus() != ProjectStatus.QUOTED) {
      throw new InvalidProjectOperationException(
              "Can only reject projects in QUOTED status. Current status: " + project.getStatus());
    }

    // Update status to rejected
    project.setStatus(ProjectStatus.REJECTED);

    Project updatedProject = projectRepository.save(project);
    log.info("Successfully rejected quote for project: {}", projectId);

    return updatedProject;
  }

  @Override
  public Project updateProgress(String projectId, ProgressUpdateDto dto) {
    log.info("Updating progress for project: {} to {}%", projectId, dto.getProgress());

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> {
              log.warn("Project {} not found", projectId);
              return new ProjectNotFoundException("Project not found");
            });

    // Can only update progress for approved or in-progress projects
    if (project.getStatus() != ProjectStatus.APPROVED &&
        project.getStatus() != ProjectStatus.IN_PROGRESS) {
      throw new InvalidProjectOperationException(
              "Can only update progress for APPROVED or IN_PROGRESS projects");
    }

    // Update progress
    project.setProgress(dto.getProgress());

    // Auto-update status based on progress
    if (dto.getProgress() > 0 && project.getStatus() == ProjectStatus.APPROVED) {
      project.setStatus(ProjectStatus.IN_PROGRESS);
    } else if (dto.getProgress() == 100) {
      project.setStatus(ProjectStatus.COMPLETED);
    }

    Project updatedProject = projectRepository.save(project);
    log.info("Successfully updated progress for project: {}", projectId);

    return updatedProject;
  }

  @Override
  public List<Project> getAllProjects() {
    log.info("Fetching all projects");
    return projectRepository.findAll();
  }
}