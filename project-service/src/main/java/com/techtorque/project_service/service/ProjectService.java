package com.techtorque.project_service.service;

import com.techtorque.project_service.dto.request.ProjectRequestDto;
import com.techtorque.project_service.dto.response.QuoteDto;
import com.techtorque.project_service.dto.request.RejectionDto;
import com.techtorque.project_service.dto.request.ProgressUpdateDto;
import com.techtorque.project_service.entity.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectService {

  Project requestNewProject(ProjectRequestDto dto, String customerId);

  List<Project> getProjectsForCustomer(String customerId);

  Optional<Project> getProjectDetails(String projectId, String userId, String userRole);

  Project submitQuoteForProject(String projectId, QuoteDto dto);

  Project acceptQuote(String projectId, String customerId);

  Project rejectQuote(String projectId, RejectionDto dto, String customerId);

  Project updateProgress(String projectId, ProgressUpdateDto dto);

  List<Project> getAllProjects();

  Project approveProject(String projectId, String adminId);

  Project rejectProject(String projectId, String reason, String adminId);
}