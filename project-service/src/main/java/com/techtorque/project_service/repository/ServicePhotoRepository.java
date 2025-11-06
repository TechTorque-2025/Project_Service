package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.ServicePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePhotoRepository extends JpaRepository<ServicePhoto, String> {

    List<ServicePhoto> findByServiceIdOrderByUploadedAtDesc(String serviceId);
}
