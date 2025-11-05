package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.ProgressPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressPhotoRepository extends JpaRepository<ProgressPhoto, String> {
    List<ProgressPhoto> findByServiceId(String serviceId);
}
