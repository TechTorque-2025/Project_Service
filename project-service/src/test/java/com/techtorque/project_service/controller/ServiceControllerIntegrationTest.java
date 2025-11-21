package com.techtorque.project_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtorque.project_service.dto.request.CompletionDto;
import com.techtorque.project_service.dto.request.CreateServiceDto;
import com.techtorque.project_service.dto.request.ServiceUpdateDto;
import com.techtorque.project_service.dto.response.NoteDto;
import com.techtorque.project_service.dto.response.InvoiceDto;
import com.techtorque.project_service.dto.response.NoteResponseDto;
import com.techtorque.project_service.dto.response.PhotoDto;
import com.techtorque.project_service.entity.InvoiceStatus;
import com.techtorque.project_service.entity.ServiceStatus;
import com.techtorque.project_service.entity.StandardService;
import com.techtorque.project_service.service.StandardServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.techtorque.project_service.config.TestSecurityConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ServiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StandardServiceService standardServiceService;

    private StandardService testService;

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
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testCreateService_Success() throws Exception {
        CreateServiceDto createDto = new CreateServiceDto();
        createDto.setAppointmentId("appointment123");
        createDto.setCustomerId("customer123");
        createDto.setEstimatedHours(2.0);

        when(standardServiceService.createServiceFromAppointment(any(CreateServiceDto.class), anyString()))
                .thenReturn(testService);

        mockMvc.perform(post("/services")
                        .header("X-User-Subject", "employee1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("service123"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testListCustomerServices_Customer() throws Exception {
        when(standardServiceService.getServicesForCustomer("customer123", null))
                .thenReturn(Arrays.asList(testService));

        mockMvc.perform(get("/services")
                        .header("X-User-Subject", "customer123")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListCustomerServices_Admin() throws Exception {
        when(standardServiceService.getAllServices())
                .thenReturn(Arrays.asList(testService));

        mockMvc.perform(get("/services")
                        .header("X-User-Subject", "admin123")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetServiceDetails_Success() throws Exception {
        when(standardServiceService.getServiceDetails(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(testService));

        mockMvc.perform(get("/services/{serviceId}", "service123")
                        .header("X-User-Subject", "customer123")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("service123"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testUpdateService_Success() throws Exception {
        ServiceUpdateDto updateDto = new ServiceUpdateDto();
        updateDto.setStatus(ServiceStatus.IN_PROGRESS);
        updateDto.setProgress(50);

        when(standardServiceService.updateService(anyString(), any(ServiceUpdateDto.class), anyString()))
                .thenReturn(testService);

        mockMvc.perform(patch("/services/{serviceId}", "service123")
                        .header("X-User-Subject", "employee1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testMarkServiceComplete_Success() throws Exception {
        CompletionDto completionDto = new CompletionDto();
        completionDto.setFinalNotes("Service completed");
        completionDto.setActualCost(new BigDecimal("1000.00"));

        InvoiceDto invoiceDto = InvoiceDto.builder()
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

        when(standardServiceService.completeService(anyString(), any(CompletionDto.class), anyString()))
                .thenReturn(invoiceDto);

        mockMvc.perform(post("/services/{serviceId}/complete", "service123")
                        .header("X-User-Subject", "employee1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-123"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetServiceInvoice_Success() throws Exception {
        InvoiceDto invoiceDto = InvoiceDto.builder()
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

        when(standardServiceService.getServiceInvoice(anyString(), anyString()))
                .thenReturn(invoiceDto);

        mockMvc.perform(get("/services/{serviceId}/invoice", "service123")
                        .header("X-User-Subject", "customer123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-123"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testAddServiceNote_Success() throws Exception {
        NoteDto noteDto = new NoteDto();
        noteDto.setNote("Test note");
        noteDto.setCustomerVisible(true);

        NoteResponseDto responseDto = NoteResponseDto.builder()
                .id("note123")
                .note("Test note")
                .employeeId("employee1")
                .isCustomerVisible(true)
                .build();

        when(standardServiceService.addServiceNote(anyString(), any(NoteDto.class), anyString()))
                .thenReturn(responseDto);

        mockMvc.perform(post("/services/{serviceId}/notes", "service123")
                        .header("X-User-Subject", "employee1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.note").value("Test note"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetServiceNotes_Success() throws Exception {
        NoteResponseDto note1 = NoteResponseDto.builder()
                .id("note1")
                .note("Note 1")
                .employeeId("employee1")
                .isCustomerVisible(true)
                .build();

        when(standardServiceService.getServiceNotes(anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList(note1));

        mockMvc.perform(get("/services/{serviceId}/notes", "service123")
                        .header("X-User-Subject", "employee1")
                        .header("X-User-Roles", "ROLE_EMPLOYEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testUploadProgressPhotos_Success() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "photo1.jpg", MediaType.IMAGE_JPEG_VALUE, "photo1".getBytes());

        PhotoDto photoDto = PhotoDto.builder()
                .id("photo1")
                .photoUrl("url1")
                .uploadedBy("employee1")
                .build();

        when(standardServiceService.uploadPhotos(anyString(), any(), anyString()))
                .thenReturn(Arrays.asList(photoDto));

        mockMvc.perform(multipart("/services/{serviceId}/photos", "service123")
                        .file(file1)
                        .header("X-User-Subject", "employee1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetProgressPhotos_Success() throws Exception {
        PhotoDto photoDto = PhotoDto.builder()
                .id("photo1")
                .photoUrl("url1")
                .uploadedBy("employee1")
                .build();

        when(standardServiceService.getPhotos(anyString()))
                .thenReturn(Arrays.asList(photoDto));

        mockMvc.perform(get("/services/{serviceId}/photos", "service123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
