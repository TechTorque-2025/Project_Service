package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, String> {
    Optional<Quote> findByProjectId(String projectId);
}
