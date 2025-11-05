package com.techtorque.project_service.service.impl;

import com.techtorque.project_service.dto.request.*;
import com.techtorque.project_service.dto.response.*;
import com.techtorque.project_service.entity.*;
import com.techtorque.project_service.exception.ServiceNotFoundException;
import com.techtorque.project_service.exception.UnauthorizedAccessException;
import com.techtorque.project_service.repository.*;
import com.techtorque.project_service.service.FileStorageService;
import com.techtorque.project_service.service.StandardServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StandardServiceServiceImpl implements StandardServiceService {

  private final ServiceRepository serviceRepository;
  private final ServiceNoteRepository serviceNoteRepository;
  private final ProgressPhotoRepository progressPhotoRepository;
  private final InvoiceRepository invoiceRepository;
  private final FileStorageService fileStorageService;

  @Override
  public StandardService createServiceFromAppointment(CreateServiceDto dto, String employeeId) {
    log.info("Creating service from appointment: {}", dto.getAppointmentId());

    // Create new standard service
    StandardService service = StandardService.builder()
            .appointmentId(dto.getAppointmentId())
            .customerId(dto.getCustomerId())
            .assignedEmployeeIds(dto.getAssignedEmployeeIds() != null ? 
                    dto.getAssignedEmployeeIds() : new HashSet<>())
            .status(ServiceStatus.CREATED)
            .progress(0)
            .hoursLogged(0)
            .estimatedCompletion(LocalDateTime.now().plusHours(dto.getEstimatedHours().longValue()))
            .build();

    StandardService savedService = serviceRepository.save(service);
    log.info("Service created successfully with ID: {}", savedService.getId());

    return savedService;
  }

  @Override
  public List<StandardService> getServicesForCustomer(String customerId, String status) {
    log.info("Fetching services for customer: {} with status filter: {}", customerId, status);
    
    List<StandardService> services = serviceRepository.findByCustomerId(customerId);
    
    if (status != null && !status.isEmpty()) {
      try {
        ServiceStatus statusEnum = ServiceStatus.valueOf(status.toUpperCase());
        services = services.stream()
                .filter(s -> s.getStatus() == statusEnum)
                .collect(Collectors.toList());
      } catch (IllegalArgumentException e) {
        log.warn("Invalid status filter provided: {}", status);
      }
    }
    
    return services;
  }

  @Override
  public Optional<StandardService> getServiceDetails(String serviceId, String userId, String userRole) {
    log.info("Fetching service {} for user: {} with role: {}", serviceId, userId, userRole);

    Optional<StandardService> serviceOpt = serviceRepository.findById(serviceId);

    if (serviceOpt.isEmpty()) {
      return Optional.empty();
    }

    StandardService service = serviceOpt.get();

    // Role-based access control
    if (userRole.contains("ADMIN") || userRole.contains("EMPLOYEE")) {
      return serviceOpt;
    } else if (userRole.contains("CUSTOMER")) {
      if (service.getCustomerId().equals(userId)) {
        return serviceOpt;
      }
    }

    log.warn("User {} with role {} attempted to access service {} without permission",
            userId, userRole, serviceId);
    return Optional.empty();
  }

  @Override
  public StandardService updateService(String serviceId, ServiceUpdateDto dto, String employeeId) {
    log.info("Updating service: {} by employee: {}", serviceId, employeeId);

    StandardService service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException("Service not found"));

    // Update fields if provided
    if (dto.getStatus() != null) {
      service.setStatus(dto.getStatus());
      log.info("Service status updated to: {}", dto.getStatus());
    }

    if (dto.getProgress() != null) {
      service.setProgress(dto.getProgress());
      log.info("Service progress updated to: {}%", dto.getProgress());
    }

    if (dto.getEstimatedCompletion() != null) {
      service.setEstimatedCompletion(dto.getEstimatedCompletion());
    }

    // If notes are provided, add them as a service note
    if (dto.getNotes() != null && !dto.getNotes().isEmpty()) {
      ServiceNote note = ServiceNote.builder()
              .serviceId(serviceId)
              .employeeId(employeeId)
              .note(dto.getNotes())
              .isCustomerVisible(true)
              .build();
      serviceNoteRepository.save(note);
      log.info("Service note added");
    }

    StandardService updatedService = serviceRepository.save(service);
    log.info("Service updated successfully");

    return updatedService;
  }

  @Override
  public InvoiceDto completeService(String serviceId, CompletionDto dto, String employeeId) {
    log.info("Completing service: {} by employee: {}", serviceId, employeeId);

    StandardService service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException("Service not found"));

    // Update service status to completed
    service.setStatus(ServiceStatus.COMPLETED);
    service.setProgress(100);
    serviceRepository.save(service);

    // Add final completion note
    ServiceNote completionNote = ServiceNote.builder()
            .serviceId(serviceId)
            .employeeId(employeeId)
            .note(dto.getFinalNotes())
            .isCustomerVisible(true)
            .build();
    serviceNoteRepository.save(completionNote);

    // Generate invoice
    Invoice invoice = generateInvoice(service, dto);
    Invoice savedInvoice = invoiceRepository.save(invoice);

    log.info("Service completed and invoice generated: {}", savedInvoice.getInvoiceNumber());

    return mapToInvoiceDto(savedInvoice);
  }

  @Override
  public NoteResponseDto addServiceNote(String serviceId, NoteDto dto, String employeeId) {
    log.info("Adding note to service: {} by employee: {}", serviceId, employeeId);

    // Verify service exists
    serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException("Service not found"));

    ServiceNote note = ServiceNote.builder()
            .serviceId(serviceId)
            .employeeId(employeeId)
            .note(dto.getNote())
            .isCustomerVisible(dto.isCustomerVisible())
            .build();

    ServiceNote savedNote = serviceNoteRepository.save(note);
    log.info("Service note added successfully");

    return mapToNoteResponseDto(savedNote);
  }

  @Override
  public List<NoteResponseDto> getServiceNotes(String serviceId, String userId, String userRole) {
    log.info("Fetching notes for service: {} by user: {} with role: {}", serviceId, userId, userRole);

    // Verify service exists and user has access
    Optional<StandardService> serviceOpt = getServiceDetails(serviceId, userId, userRole);
    if (serviceOpt.isEmpty()) {
      throw new UnauthorizedAccessException("You don't have permission to view this service");
    }

    List<ServiceNote> notes;
    if (userRole.contains("CUSTOMER")) {
      // Customers can only see customer-visible notes
      notes = serviceNoteRepository.findByServiceIdAndIsCustomerVisible(serviceId, true);
    } else {
      // Employees and admins can see all notes
      notes = serviceNoteRepository.findByServiceId(serviceId);
    }

    return notes.stream()
            .map(this::mapToNoteResponseDto)
            .collect(Collectors.toList());
  }

  @Override
  public List<PhotoDto> uploadPhotos(String serviceId, MultipartFile[] files, String employeeId) {
    log.info("Uploading {} photos for service: {}", files.length, serviceId);

    // Verify service exists
    serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException("Service not found"));

    List<String> fileUrls = fileStorageService.storeFiles(files, serviceId);
    List<ProgressPhoto> photos = new ArrayList<>();

    for (String fileUrl : fileUrls) {
      ProgressPhoto photo = ProgressPhoto.builder()
              .serviceId(serviceId)
              .photoUrl(fileUrl)
              .uploadedBy(employeeId)
              .build();
      photos.add(photo);
    }

    List<ProgressPhoto> savedPhotos = progressPhotoRepository.saveAll(photos);
    log.info("Successfully uploaded {} photos", savedPhotos.size());

    return savedPhotos.stream()
            .map(this::mapToPhotoDto)
            .collect(Collectors.toList());
  }

  @Override
  public List<PhotoDto> getPhotos(String serviceId) {
    log.info("Fetching photos for service: {}", serviceId);

    List<ProgressPhoto> photos = progressPhotoRepository.findByServiceId(serviceId);
    
    return photos.stream()
            .map(this::mapToPhotoDto)
            .collect(Collectors.toList());
  }

  @Override
  public InvoiceDto getServiceInvoice(String serviceId, String userId) {
    log.info("Fetching invoice for service: {}", serviceId);

    // Verify service exists and user has access
    StandardService service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException("Service not found"));

    // Check access permission
    if (!service.getCustomerId().equals(userId)) {
      throw new UnauthorizedAccessException("You don't have permission to view this invoice");
    }

    Invoice invoice = invoiceRepository.findByServiceId(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException("Invoice not found for this service"));

    return mapToInvoiceDto(invoice);
  }

  // Helper methods

  private Invoice generateInvoice(StandardService service, CompletionDto dto) {
    String invoiceNumber = generateInvoiceNumber();
    
    BigDecimal subtotal = dto.getActualCost();
    BigDecimal taxRate = new BigDecimal("0.15"); // 15% tax
    BigDecimal taxAmount = subtotal.multiply(taxRate);
    BigDecimal totalAmount = subtotal.add(taxAmount);

    Invoice invoice = Invoice.builder()
            .invoiceNumber(invoiceNumber)
            .serviceId(service.getId())
            .customerId(service.getCustomerId())
            .items(new ArrayList<>())
            .subtotal(subtotal)
            .taxAmount(taxAmount)
            .totalAmount(totalAmount)
            .status(InvoiceStatus.PENDING)
            .build();

    // Add main service item
    InvoiceItem mainItem = InvoiceItem.builder()
            .invoice(invoice)
            .description("Service Completion - " + service.getAppointmentId())
            .quantity(1)
            .unitPrice(dto.getActualCost())
            .amount(dto.getActualCost())
            .build();
    invoice.getItems().add(mainItem);

    // Add additional charges if any
    if (dto.getAdditionalCharges() != null && !dto.getAdditionalCharges().isEmpty()) {
      for (InvoiceItemDto itemDto : dto.getAdditionalCharges()) {
        InvoiceItem additionalItem = InvoiceItem.builder()
                .invoice(invoice)
                .description(itemDto.getDescription())
                .quantity(itemDto.getQuantity())
                .unitPrice(itemDto.getUnitPrice())
                .amount(itemDto.getAmount())
                .build();
        invoice.getItems().add(additionalItem);
        
        subtotal = subtotal.add(itemDto.getAmount());
      }
      
      // Recalculate totals with additional items
      taxAmount = subtotal.multiply(taxRate);
      totalAmount = subtotal.add(taxAmount);
      invoice.setSubtotal(subtotal);
      invoice.setTaxAmount(taxAmount);
      invoice.setTotalAmount(totalAmount);
    }

    return invoice;
  }

  private String generateInvoiceNumber() {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    return "INV-" + timestamp;
  }

  private InvoiceDto mapToInvoiceDto(Invoice invoice) {
    return InvoiceDto.builder()
            .id(invoice.getId())
            .invoiceNumber(invoice.getInvoiceNumber())
            .serviceId(invoice.getServiceId())
            .customerId(invoice.getCustomerId())
            .items(invoice.getItems().stream()
                    .map(this::mapToInvoiceItemDto)
                    .collect(Collectors.toList()))
            .subtotal(invoice.getSubtotal())
            .taxAmount(invoice.getTaxAmount())
            .totalAmount(invoice.getTotalAmount())
            .status(invoice.getStatus())
            .paidAt(invoice.getPaidAt())
            .createdAt(invoice.getCreatedAt())
            .build();
  }

  private InvoiceItemDto mapToInvoiceItemDto(InvoiceItem item) {
    return InvoiceItemDto.builder()
            .id(item.getId())
            .description(item.getDescription())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .amount(item.getAmount())
            .build();
  }

  private NoteResponseDto mapToNoteResponseDto(ServiceNote note) {
    return NoteResponseDto.builder()
            .id(note.getId())
            .note(note.getNote())
            .employeeId(note.getEmployeeId())
            .isCustomerVisible(note.isCustomerVisible())
            .createdAt(note.getCreatedAt())
            .build();
  }

  private PhotoDto mapToPhotoDto(ProgressPhoto photo) {
    return PhotoDto.builder()
            .id(photo.getId())
            .photoUrl(photo.getPhotoUrl())
            .description(photo.getDescription())
            .uploadedBy(photo.getUploadedBy())
            .uploadedAt(photo.getUploadedAt())
            .build();
  }
}