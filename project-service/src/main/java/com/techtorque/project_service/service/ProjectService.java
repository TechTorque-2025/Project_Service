package com.techtorque.project_service.service;

import com.techtorque.project_service.entity.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectService {

  Project requestNewProject(/* ProjectRequestDto dto, */ String customerId);

  List<Project> getProjectsForCustomer(String customerId);

  Optional<Project> getProjectDetails(String projectId, String userId, String userRole);

  Project submitQuoteForProject(String projectId /*, QuoteDto dto */);

  Project acceptQuote(String projectId, String customerId);

  Project rejectQuote(String projectId, /* RejectionDto dto, */ String customerId);
}