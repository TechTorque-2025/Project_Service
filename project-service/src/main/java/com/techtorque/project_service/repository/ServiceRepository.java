package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.StandardService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<StandardService, String> {
  List<StandardService> findByCustomerId(String customerId);
  List<StandardService> findByAssignedEmployeeIdsContains(String employeeId);
}