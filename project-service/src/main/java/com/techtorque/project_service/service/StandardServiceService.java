package com.techtorque.project_service.service;

import com.techtorque.project_service.dto.request.*;
import com.techtorque.project_service.dto.response.*;
import com.techtorque.project_service.entity.StandardService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface StandardServiceService {

  StandardService createServiceFromAppointment(CreateServiceDto dto, String employeeId);

  List<StandardService> getServicesForCustomer(String customerId, String status);
  
  List<StandardService> getAllServices(); // For admin/employee to see all services

  Optional<StandardService> getServiceDetails(String serviceId, String userId, String userRole);

  StandardService updateService(String serviceId, ServiceUpdateDto dto, String employeeId);

  InvoiceDto completeService(String serviceId, CompletionDto dto, String employeeId);

  NoteResponseDto addServiceNote(String serviceId, NoteDto dto, String employeeId);

  List<NoteResponseDto> getServiceNotes(String serviceId, String userId, String userRole);

  List<PhotoDto> uploadPhotos(String serviceId, MultipartFile[] files, String employeeId);

  List<PhotoDto> getPhotos(String serviceId);

  InvoiceDto getServiceInvoice(String serviceId, String userId);
}