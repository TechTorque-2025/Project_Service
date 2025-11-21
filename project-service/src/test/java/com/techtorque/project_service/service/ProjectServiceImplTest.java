package com.techtorque.project_service.service;

import com.techtorque.project_service.client.AppointmentClient;
import com.techtorque.project_service.client.NotificationClient;
import com.techtorque.project_service.dto.request.ProgressUpdateDto;
import com.techtorque.project_service.dto.request.ProjectRequestDto;
import com.techtorque.project_service.dto.request.RejectionDto;
import com.techtorque.project_service.dto.response.QuoteDto;
import com.techtorque.project_service.entity.Project;
import com.techtorque.project_service.entity.ProjectStatus;
import com.techtorque.project_service.exception.InvalidProjectOperationException;
import com.techtorque.project_service.exception.ProjectNotFoundException;
import com.techtorque.project_service.repository.ProjectRepository;
import com.techtorque.project_service.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AppointmentClient appointmentClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project testProject;
    private ProjectRequestDto requestDto;

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

        requestDto = new ProjectRequestDto();
        requestDto.setVehicleId("vehicle456");
        requestDto.setProjectType("CUSTOM_MODIFICATION");
        requestDto.setDescription("Custom body kit installation");
        requestDto.setDesiredCompletionDate("2025-12-31");
        requestDto.setBudget(new BigDecimal("5000.00"));
    }

    @Test
    void testRequestNewProject_Success() {
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.requestNewProject(requestDto, "customer123");

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo("customer123");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.REQUESTED);
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testGetProjectsForCustomer() {
        Project project2 = Project.builder()
                .id("project456")
                .customerId("customer123")
                .vehicleId("vehicle789")
                .projectType("PAINT_JOB")
                .description("Custom paint")
                .status(ProjectStatus.QUOTED)
                .progress(0)
                .build();

        when(projectRepository.findByCustomerId("customer123"))
                .thenReturn(Arrays.asList(testProject, project2));

        List<Project> results = projectService.getProjectsForCustomer("customer123");

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(p -> p.getCustomerId().equals("customer123"));
        verify(projectRepository, times(1)).findByCustomerId("customer123");
    }

    @Test
    void testGetProjectDetails_Admin_Success() {
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));

        Optional<Project> result = projectService.getProjectDetails("project123", "admin1", "ROLE_ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("project123");
    }

    @Test
    void testGetProjectDetails_Customer_OwnProject_Success() {
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));

        Optional<Project> result = projectService.getProjectDetails("project123", "customer123", "ROLE_CUSTOMER");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("project123");
    }

    @Test
    void testGetProjectDetails_Customer_OtherProject_Denied() {
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));

        Optional<Project> result = projectService.getProjectDetails("project123", "otherCustomer", "ROLE_CUSTOMER");

        assertThat(result).isEmpty();
    }

    @Test
    void testSubmitQuoteForProject_Success() {
        QuoteDto quoteDto = new QuoteDto();
        quoteDto.setQuoteAmount(new BigDecimal("5500.00"));

        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.submitQuoteForProject("project123", quoteDto);

        assertThat(result).isNotNull();
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testSubmitQuoteForProject_ProjectNotFound() {
        QuoteDto quoteDto = new QuoteDto();
        quoteDto.setQuoteAmount(new BigDecimal("5500.00"));

        when(projectRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.submitQuoteForProject("nonexistent", quoteDto))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void testSubmitQuoteForProject_InvalidStatus() {
        QuoteDto quoteDto = new QuoteDto();
        quoteDto.setQuoteAmount(new BigDecimal("5500.00"));

        testProject.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));

        assertThatThrownBy(() -> projectService.submitQuoteForProject("project123", quoteDto))
                .isInstanceOf(InvalidProjectOperationException.class);
    }

    @Test
    void testAcceptQuote_Success() {
        testProject.setStatus(ProjectStatus.QUOTED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.acceptQuote("project123", "customer123");

        assertThat(result).isNotNull();
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testAcceptQuote_UnauthorizedCustomer() {
        testProject.setStatus(ProjectStatus.QUOTED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));

        assertThatThrownBy(() -> projectService.acceptQuote("project123", "wrongCustomer"))
                .isInstanceOf(InvalidProjectOperationException.class)
                .hasMessageContaining("don't have permission");
    }

    @Test
    void testAcceptQuote_InvalidStatus() {
        testProject.setStatus(ProjectStatus.REQUESTED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));

        assertThatThrownBy(() -> projectService.acceptQuote("project123", "customer123"))
                .isInstanceOf(InvalidProjectOperationException.class);
    }

    @Test
    void testRejectQuote_Success() {
        RejectionDto rejectionDto = new RejectionDto();
        rejectionDto.setReason("Price too high");

        testProject.setStatus(ProjectStatus.QUOTED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.rejectQuote("project123", rejectionDto, "customer123");

        assertThat(result).isNotNull();
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testUpdateProgress_Success() {
        ProgressUpdateDto progressDto = new ProgressUpdateDto();
        progressDto.setProgress(50);

        testProject.setStatus(ProjectStatus.APPROVED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.updateProgress("project123", progressDto);

        assertThat(result).isNotNull();
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testUpdateProgress_AutoStatusChange_ToInProgress() {
        ProgressUpdateDto progressDto = new ProgressUpdateDto();
        progressDto.setProgress(25);

        testProject.setStatus(ProjectStatus.APPROVED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
            return saved;
        });

        projectService.updateProgress("project123", progressDto);

        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testUpdateProgress_AutoStatusChange_ToCompleted() {
        ProgressUpdateDto progressDto = new ProgressUpdateDto();
        progressDto.setProgress(100);

        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
            return saved;
        });

        projectService.updateProgress("project123", progressDto);

        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testGetAllProjects() {
        Project project2 = Project.builder()
                .id("project456")
                .customerId("customer456")
                .vehicleId("vehicle789")
                .projectType("ENGINE_SWAP")
                .description("Engine swap")
                .status(ProjectStatus.REQUESTED)
                .progress(0)
                .build();

        when(projectRepository.findAll()).thenReturn(Arrays.asList(testProject, project2));

        List<Project> results = projectService.getAllProjects();

        assertThat(results).hasSize(2);
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void testApproveProject_Success() {
        testProject.setStatus(ProjectStatus.REQUESTED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        doNothing().when(notificationClient).sendProjectNotification(
                anyString(), anyString(), anyString(), anyString(), anyString());

        Project result = projectService.approveProject("project123", "admin1");

        assertThat(result).isNotNull();
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(notificationClient, times(1)).sendProjectNotification(
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testApproveProject_WithAppointment() {
        testProject.setStatus(ProjectStatus.REQUESTED);
        testProject.setAppointmentId("appointment123");
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        doNothing().when(notificationClient).sendProjectNotification(
                anyString(), anyString(), anyString(), anyString(), anyString());
        doNothing().when(appointmentClient).confirmAppointment(anyString(), anyString());

        Project result = projectService.approveProject("project123", "admin1");

        assertThat(result).isNotNull();
        verify(appointmentClient, times(1)).confirmAppointment("appointment123", "admin1");
    }

    @Test
    void testRejectProject_Success() {
        testProject.setStatus(ProjectStatus.REQUESTED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        doNothing().when(notificationClient).sendProjectNotification(
                anyString(), anyString(), anyString(), anyString(), anyString());

        Project result = projectService.rejectProject("project123", "Not feasible", "admin1");

        assertThat(result).isNotNull();
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(notificationClient, times(1)).sendProjectNotification(
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testRejectProject_WithAppointment() {
        testProject.setStatus(ProjectStatus.REQUESTED);
        testProject.setAppointmentId("appointment123");
        when(projectRepository.findById("project123")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        doNothing().when(notificationClient).sendProjectNotification(
                anyString(), anyString(), anyString(), anyString(), anyString());
        doNothing().when(appointmentClient).cancelAppointment(anyString(), anyString());

        Project result = projectService.rejectProject("project123", "Not feasible", "admin1");

        assertThat(result).isNotNull();
        verify(appointmentClient, times(1)).cancelAppointment("appointment123", "admin1");
    }
}
