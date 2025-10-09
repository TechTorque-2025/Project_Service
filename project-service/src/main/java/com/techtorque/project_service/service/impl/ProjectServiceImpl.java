package com.techtorque.project_service.service.impl;

import com.techtorque.project_service.entity.Project;
import com.techtorque.project_service.repository.ProjectRepository;
import com.techtorque.project_service.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

  private final ProjectRepository projectRepository;

  public ProjectServiceImpl(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Override
  public Project requestNewProject(/* ProjectRequestDto dto, */ String customerId) {
    // TODO: Logic for creating a new project request.
    return null;
  }

  @Override
  public List<Project> getProjectsForCustomer(String customerId) {
    // TODO: Call projectRepository.findByCustomerId(customerId).
    return List.of();
  }

  @Override
  public Optional<Project> getProjectDetails(String projectId, String userId, String userRole) {
    // TODO: Find project by ID and verify user has permission to view.
    return Optional.empty();
  }

  @Override
  public Project submitQuoteForProject(String projectId /*, QuoteDto dto */) {
    // TODO: Find project, associate the quote details, update status to QUOTED, and save.
    return null;
  }

  @Override
  public Project acceptQuote(String projectId, String customerId) {
    // TODO: Find project, verify customer ownership, update status to APPROVED.
    // Potentially make an inter-service call to the Billing Service to generate a deposit invoice.
    return null;
  }

  @Override
  public Project rejectQuote(String projectId, /* RejectionDto dto, */ String customerId) {
    // TODO: Find project, verify customer ownership, update status to REJECTED, and save the reason.
    return null;
  }
}