package com.techtorque.project_service.service;

import com.techtorque.project_service.dto.request.CompletionDto;
import com.techtorque.project_service.dto.request.CreateServiceDto;
import com.techtorque.project_service.dto.request.ServiceUpdateDto;
import com.techtorque.project_service.dto.response.NoteDto;
import com.techtorque.project_service.dto.response.InvoiceDto;
import com.techtorque.project_service.dto.response.InvoiceItemDto;
import com.techtorque.project_service.dto.response.NoteResponseDto;
import com.techtorque.project_service.dto.response.PhotoDto;
import com.techtorque.project_service.entity.*;
import com.techtorque.project_service.exception.ServiceNotFoundException;
import com.techtorque.project_service.exception.UnauthorizedAccessException;
import com.techtorque.project_service.repository.*;
import com.techtorque.project_service.service.impl.StandardServiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandardServiceServiceImplTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceNoteRepository serviceNoteRepository;

    @Mock
    private ProgressPhotoRepository progressPhotoRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private StandardServiceServiceImpl standardServiceService;

    private StandardService testService;
    private CreateServiceDto createDto;

    @BeforeEach
    void setUp() {
        Set<String> assignedEmployees = new HashSet<>();
        assignedEmployees.add("employee1");

        testService = StandardService.builder()
                .id("service123")
                .appointmentId("appointment123")
                .customerId("customer123")
                .assignedEmployeeIds(assignedEmployees)
                .status(ServiceStatus.CREATED)
                .progress(0)
                .hoursLogged(0.0)
                .estimatedCompletion(LocalDateTime.now().plusHours(2))
                .build();

        createDto = new CreateServiceDto();
        createDto.setAppointmentId("appointment123");
        createDto.setCustomerId("customer123");
        createDto.setAssignedEmployeeIds(assignedEmployees);
        createDto.setEstimatedHours(2.0);
    }

    @Test
    void testCreateServiceFromAppointment_Success() {
        when(serviceRepository.save(any(StandardService.class))).thenReturn(testService);

        StandardService result = standardServiceService.createServiceFromAppointment(createDto, "employee1");

        assertThat(result).isNotNull();
        assertThat(result.getAppointmentId()).isEqualTo("appointment123");
        assertThat(result.getStatus()).isEqualTo(ServiceStatus.CREATED);
        verify(serviceRepository, times(1)).save(any(StandardService.class));
    }

    @Test
    void testGetServicesForCustomer_NoFilter() {
        StandardService service2 = StandardService.builder()
                .id("service456")
                .appointmentId("appointment456")
                .customerId("customer123")
                .assignedEmployeeIds(new HashSet<>())
                .status(ServiceStatus.IN_PROGRESS)
                .progress(50)
                .hoursLogged(1.5)
                .build();

        when(serviceRepository.findByCustomerId("customer123"))
                .thenReturn(Arrays.asList(testService, service2));

        List<StandardService> results = standardServiceService.getServicesForCustomer("customer123", null);

        assertThat(results).hasSize(2);
        verify(serviceRepository, times(1)).findByCustomerId("customer123");
    }

    @Test
    void testGetServicesForCustomer_WithStatusFilter() {
        when(serviceRepository.findByCustomerId("customer123"))
                .thenReturn(Arrays.asList(testService));

        List<StandardService> results = standardServiceService.getServicesForCustomer("customer123", "CREATED");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(ServiceStatus.CREATED);
    }

    @Test
    void testGetAllServices() {
        when(serviceRepository.findAll()).thenReturn(Arrays.asList(testService));

        List<StandardService> results = standardServiceService.getAllServices();

        assertThat(results).hasSize(1);
        verify(serviceRepository, times(1)).findAll();
    }

    @Test
    void testGetServiceDetails_Admin_Success() {
        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));

        Optional<StandardService> result = standardServiceService.getServiceDetails("service123", "admin1", "ROLE_ADMIN");

        assertThat(result).isPresent();
    }

    @Test
    void testGetServiceDetails_Customer_OwnService_Success() {
        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));

        Optional<StandardService> result = standardServiceService.getServiceDetails("service123", "customer123", "ROLE_CUSTOMER");

        assertThat(result).isPresent();
    }

    @Test
    void testGetServiceDetails_Customer_OtherService_Denied() {
        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));

        Optional<StandardService> result = standardServiceService.getServiceDetails("service123", "otherCustomer", "ROLE_CUSTOMER");

        assertThat(result).isEmpty();
    }

    @Test
    void testUpdateService_Success() {
        ServiceUpdateDto updateDto = new ServiceUpdateDto();
        updateDto.setStatus(ServiceStatus.IN_PROGRESS);
        updateDto.setProgress(50);
        updateDto.setNotes("Work in progress");

        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(StandardService.class))).thenReturn(testService);
        when(serviceNoteRepository.save(any(ServiceNote.class))).thenReturn(new ServiceNote());

        StandardService result = standardServiceService.updateService("service123", updateDto, "employee1");

        assertThat(result).isNotNull();
        verify(serviceRepository, times(1)).save(any(StandardService.class));
        verify(serviceNoteRepository, times(1)).save(any(ServiceNote.class));
    }

    @Test
    void testUpdateService_ServiceNotFound() {
        ServiceUpdateDto updateDto = new ServiceUpdateDto();
        updateDto.setStatus(ServiceStatus.IN_PROGRESS);

        when(serviceRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> standardServiceService.updateService("nonexistent", updateDto, "employee1"))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void testCompleteService_Success() {
        CompletionDto completionDto = new CompletionDto();
        completionDto.setFinalNotes("Service completed successfully");
        completionDto.setActualCost(new BigDecimal("1000.00"));
        completionDto.setAdditionalCharges(new ArrayList<>());

        Invoice invoice = Invoice.builder()
                .id("invoice123")
                .invoiceNumber("INV-123")
                .serviceId("service123")
                .customerId("customer123")
                .items(new ArrayList<>())
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("1150.00"))
                .status(InvoiceStatus.PENDING)
                .build();

        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(StandardService.class))).thenReturn(testService);
        when(serviceNoteRepository.save(any(ServiceNote.class))).thenReturn(new ServiceNote());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        InvoiceDto result = standardServiceService.completeService("service123", completionDto, "employee1");

        assertThat(result).isNotNull();
        assertThat(result.getInvoiceNumber()).isEqualTo("INV-123");
        verify(serviceRepository, times(1)).save(any(StandardService.class));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testAddServiceNote_Success() {
        NoteDto noteDto = new NoteDto();
        noteDto.setNote("Customer notified about delay");
        noteDto.setCustomerVisible(true);

        ServiceNote savedNote = ServiceNote.builder()
                .id("note123")
                .serviceId("service123")
                .employeeId("employee1")
                .note("Customer notified about delay")
                .isCustomerVisible(true)
                .build();

        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));
        when(serviceNoteRepository.save(any(ServiceNote.class))).thenReturn(savedNote);

        NoteResponseDto result = standardServiceService.addServiceNote("service123", noteDto, "employee1");

        assertThat(result).isNotNull();
        assertThat(result.getNote()).isEqualTo("Customer notified about delay");
        verify(serviceNoteRepository, times(1)).save(any(ServiceNote.class));
    }

    @Test
    void testAddServiceNote_ServiceNotFound() {
        NoteDto noteDto = new NoteDto();
        noteDto.setNote("Test note");

        when(serviceRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> standardServiceService.addServiceNote("nonexistent", noteDto, "employee1"))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void testGetServiceNotes_Customer_OnlyVisible() {
        ServiceNote visibleNote = ServiceNote.builder()
                .id("note1")
                .serviceId("service123")
                .employeeId("employee1")
                .note("Visible note")
                .isCustomerVisible(true)
                .build();

        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));
        when(serviceNoteRepository.findByServiceIdAndIsCustomerVisible("service123", true))
                .thenReturn(Arrays.asList(visibleNote));

        List<NoteResponseDto> results = standardServiceService.getServiceNotes("service123", "customer123", "ROLE_CUSTOMER");

        assertThat(results).hasSize(1);
        verify(serviceNoteRepository, times(1)).findByServiceIdAndIsCustomerVisible("service123", true);
    }

    @Test
    void testGetServiceNotes_Employee_AllNotes() {
        ServiceNote note1 = ServiceNote.builder()
                .id("note1")
                .serviceId("service123")
                .employeeId("employee1")
                .note("Internal note")
                .isCustomerVisible(false)
                .build();

        ServiceNote note2 = ServiceNote.builder()
                .id("note2")
                .serviceId("service123")
                .employeeId("employee1")
                .note("Visible note")
                .isCustomerVisible(true)
                .build();

        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));
        when(serviceNoteRepository.findByServiceId("service123"))
                .thenReturn(Arrays.asList(note1, note2));

        List<NoteResponseDto> results = standardServiceService.getServiceNotes("service123", "employee1", "ROLE_EMPLOYEE");

        assertThat(results).hasSize(2);
        verify(serviceNoteRepository, times(1)).findByServiceId("service123");
    }

    @Test
    void testGetServiceNotes_UnauthorizedAccess() {
        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));

        assertThatThrownBy(() -> standardServiceService.getServiceNotes("service123", "otherCustomer", "ROLE_CUSTOMER"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void testUploadPhotos_Success() {
        MultipartFile[] files = new MultipartFile[2];

        List<String> fileUrls = Arrays.asList("url1", "url2");
        ProgressPhoto photo1 = ProgressPhoto.builder()
                .id("photo1")
                .serviceId("service123")
                .photoUrl("url1")
                .uploadedBy("employee1")
                .build();
        ProgressPhoto photo2 = ProgressPhoto.builder()
                .id("photo2")
                .serviceId("service123")
                .photoUrl("url2")
                .uploadedBy("employee1")
                .build();

        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));
        when(fileStorageService.storeFiles(files, "service123")).thenReturn(fileUrls);
        when(progressPhotoRepository.saveAll(anyList())).thenReturn(Arrays.asList(photo1, photo2));

        List<PhotoDto> results = standardServiceService.uploadPhotos("service123", files, "employee1");

        assertThat(results).hasSize(2);
        verify(fileStorageService, times(1)).storeFiles(files, "service123");
        verify(progressPhotoRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGetPhotos() {
        ProgressPhoto photo1 = ProgressPhoto.builder()
                .id("photo1")
                .serviceId("service123")
                .photoUrl("url1")
                .uploadedBy("employee1")
                .build();

        when(progressPhotoRepository.findByServiceId("service123"))
                .thenReturn(Arrays.asList(photo1));

        List<PhotoDto> results = standardServiceService.getPhotos("service123");

        assertThat(results).hasSize(1);
        verify(progressPhotoRepository, times(1)).findByServiceId("service123");
    }

    @Test
    void testGetServiceInvoice_Success() {
        Invoice invoice = Invoice.builder()
                .id("invoice123")
                .invoiceNumber("INV-123")
                .serviceId("service123")
                .customerId("customer123")
                .items(new ArrayList<>())
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("1150.00"))
                .status(InvoiceStatus.PENDING)
                .build();

        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));
        when(invoiceRepository.findByServiceId("service123")).thenReturn(Optional.of(invoice));

        InvoiceDto result = standardServiceService.getServiceInvoice("service123", "customer123");

        assertThat(result).isNotNull();
        assertThat(result.getInvoiceNumber()).isEqualTo("INV-123");
    }

    @Test
    void testGetServiceInvoice_UnauthorizedAccess() {
        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));

        assertThatThrownBy(() -> standardServiceService.getServiceInvoice("service123", "otherCustomer"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void testGetServiceInvoice_InvoiceNotFound() {
        when(serviceRepository.findById("service123")).thenReturn(Optional.of(testService));
        when(invoiceRepository.findByServiceId("service123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> standardServiceService.getServiceInvoice("service123", "customer123"))
                .isInstanceOf(ServiceNotFoundException.class);
    }
}
