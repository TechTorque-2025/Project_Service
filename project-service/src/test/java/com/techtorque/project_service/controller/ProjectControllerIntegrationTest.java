package com.techtorque.project_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtorque.project_service.dto.request.ProgressUpdateDto;
import com.techtorque.project_service.dto.request.ProjectRequestDto;
import com.techtorque.project_service.dto.request.RejectionDto;
import com.techtorque.project_service.dto.response.QuoteDto;
import com.techtorque.project_service.entity.Project;
import com.techtorque.project_service.entity.ProjectStatus;
import com.techtorque.project_service.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.techtorque.project_service.config.TestSecurityConfig;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id("project123")
                .customerId("customer123")
                .vehicleId("vehicle456")
                .projectType("CUSTOM_MODIFICATION")
                .description("Custom body kit installation")
                .desiredCompletionDate("2025-12-31")
                .budget(new BigDecimal("5000.00"))
                .status(ProjectStatus.REQUESTED)
                .progress(0)
                .build();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testRequestModification_Success() throws Exception {
        ProjectRequestDto requestDto = new ProjectRequestDto();
        requestDto.setVehicleId("vehicle456");
        requestDto.setProjectType("CUSTOM_MODIFICATION");
        requestDto.setDescription("Custom body kit installation");
        requestDto.setDesiredCompletionDate("2025-12-31");
        requestDto.setBudget(new BigDecimal("5000.00"));

        when(projectService.requestNewProject(any(ProjectRequestDto.class), anyString()))
                .thenReturn(testProject);

        mockMvc.perform(post("/projects")
                        .header("X-User-Subject", "customer123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("project123"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testListCustomerProjects_Customer() throws Exception {
        when(projectService.getProjectsForCustomer("customer123"))
                .thenReturn(Arrays.asList(testProject));

        mockMvc.perform(get("/projects")
                        .header("X-User-Subject", "customer123")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListCustomerProjects_Admin() throws Exception {
        when(projectService.getAllProjects())
                .thenReturn(Arrays.asList(testProject));

        mockMvc.perform(get("/projects")
                        .header("X-User-Subject", "admin123")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetProjectDetails_Success() throws Exception {
        when(projectService.getProjectDetails(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(testProject));

        mockMvc.perform(get("/projects/{projectId}", "project123")
                        .header("X-User-Subject", "customer123")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("project123"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testSubmitQuote_Success() throws Exception {
        QuoteDto quoteDto = new QuoteDto();
        quoteDto.setQuoteAmount(new BigDecimal("5500.00"));

        testProject.setStatus(ProjectStatus.QUOTED);
        when(projectService.submitQuoteForProject(anyString(), any(QuoteDto.class)))
                .thenReturn(testProject);

        mockMvc.perform(put("/projects/{projectId}/quote", "project123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quoteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testAcceptQuote_Success() throws Exception {
        testProject.setStatus(ProjectStatus.APPROVED);
        when(projectService.acceptQuote(anyString(), anyString()))
                .thenReturn(testProject);

        mockMvc.perform(post("/projects/{projectId}/accept", "project123")
                        .header("X-User-Subject", "customer123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testRejectQuote_Success() throws Exception {
        RejectionDto rejectionDto = new RejectionDto();
        rejectionDto.setReason("Price too high");

        testProject.setStatus(ProjectStatus.REJECTED);
        when(projectService.rejectQuote(anyString(), any(RejectionDto.class), anyString()))
                .thenReturn(testProject);

        mockMvc.perform(post("/projects/{projectId}/reject", "project123")
                        .header("X-User-Subject", "customer123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testUpdateProgress_Success() throws Exception {
        ProgressUpdateDto progressDto = new ProgressUpdateDto();
        progressDto.setProgress(50);

        testProject.setProgress(50);
        when(projectService.updateProgress(anyString(), any(ProgressUpdateDto.class)))
                .thenReturn(testProject);

        mockMvc.perform(put("/projects/{projectId}/progress", "project123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListAllProjects_Success() throws Exception {
        when(projectService.getAllProjects())
                .thenReturn(Arrays.asList(testProject));

        mockMvc.perform(get("/projects/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testApproveProject_Success() throws Exception {
        testProject.setStatus(ProjectStatus.APPROVED);
        when(projectService.approveProject(anyString(), anyString()))
                .thenReturn(testProject);

        mockMvc.perform(post("/projects/{projectId}/approve", "project123")
                        .header("X-User-Subject", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRejectProject_Success() throws Exception {
        testProject.setStatus(ProjectStatus.REJECTED);
        when(projectService.rejectProject(anyString(), anyString(), anyString()))
                .thenReturn(testProject);

        mockMvc.perform(post("/projects/{projectId}/admin/reject", "project123")
                        .header("X-User-Subject", "admin123")
                        .param("reason", "Not feasible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }
}
