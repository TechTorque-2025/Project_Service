package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
  List<Project> findByCustomerId(String customerId);
}