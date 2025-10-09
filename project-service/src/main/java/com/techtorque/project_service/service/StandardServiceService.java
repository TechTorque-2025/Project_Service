package com.techtorque.project_service.service;

import com.techtorque.project_service.entity.StandardService;
import java.util.List;
import java.util.Optional;

// Using a more descriptive name to avoid confusion with the @Service annotation
public interface StandardServiceService {

  List<StandardService> getServicesForCustomer(String customerId, String status);

  Optional<StandardService> getServiceDetails(String serviceId, String userId, String userRole);

  StandardService updateService(String serviceId, /* ServiceUpdateDto dto, */ String employeeId);

  StandardService completeService(String serviceId /* ,CompletionDto dto */);

  void addServiceNote(String serviceId, /* NoteDto dto, */ String employeeId);

  List<?> getServiceNotes(String serviceId, String userId, String userRole); // Return type would be a list of Note DTOs

  void uploadPhotos(String serviceId /*, MultipartFile[] files */);

  List<?> getPhotos(String serviceId); // Return type would be a list of Photo DTOs
}