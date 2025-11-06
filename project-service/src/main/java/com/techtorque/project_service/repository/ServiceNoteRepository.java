package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.ServiceNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceNoteRepository extends JpaRepository<ServiceNote, String> {

    List<ServiceNote> findByServiceIdOrderByCreatedAtDesc(String serviceId);

    List<ServiceNote> findByServiceIdAndIsInternalFalseOrderByCreatedAtDesc(String serviceId);
}
