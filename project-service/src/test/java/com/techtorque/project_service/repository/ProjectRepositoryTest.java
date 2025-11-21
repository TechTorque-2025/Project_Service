package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.Project;
import com.techtorque.project_service.entity.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();

        testProject = Project.builder()
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
    void testSaveProject() {
        Project saved = projectRepository.save(testProject);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo("customer123");
        assertThat(saved.getProjectType()).isEqualTo("CUSTOM_MODIFICATION");
        assertThat(saved.getStatus()).isEqualTo(ProjectStatus.REQUESTED);
    }

    @Test
    void testFindById() {
        projectRepository.save(testProject);

        Optional<Project> found = projectRepository.findById(testProject.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo("customer123");
    }

    @Test
    void testFindByCustomerId() {
        Project project2 = Project.builder()
                .customerId("customer123")
                .vehicleId("vehicle789")
                .projectType("PAINT_JOB")
                .description("Custom paint job")
                .desiredCompletionDate("2025-11-30")
                .budget(new BigDecimal("3000.00"))
                .status(ProjectStatus.QUOTED)
                .progress(0)
                .build();

        Project project3 = Project.builder()
                .customerId("differentCustomer")
                .vehicleId("vehicle999")
                .projectType("ENGINE_SWAP")
                .description("Engine swap")
                .desiredCompletionDate("2026-01-15")
                .budget(new BigDecimal("10000.00"))
                .status(ProjectStatus.REQUESTED)
                .progress(0)
                .build();

        projectRepository.save(testProject);
        projectRepository.save(project2);
        projectRepository.save(project3);

        List<Project> customerProjects = projectRepository.findByCustomerId("customer123");

        assertThat(customerProjects).hasSize(2);
        assertThat(customerProjects).allMatch(p -> p.getCustomerId().equals("customer123"));
    }

    @Test
    void testUpdateProject() {
        projectRepository.save(testProject);

        testProject.setStatus(ProjectStatus.APPROVED);
        testProject.setProgress(50);
        Project updated = projectRepository.save(testProject);

        assertThat(updated.getStatus()).isEqualTo(ProjectStatus.APPROVED);
        assertThat(updated.getProgress()).isEqualTo(50);
    }

    @Test
    void testDeleteProject() {
        projectRepository.save(testProject);
        String projectId = testProject.getId();

        projectRepository.deleteById(projectId);

        Optional<Project> deleted = projectRepository.findById(projectId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testFindAll() {
        Project project2 = Project.builder()
                .customerId("customer456")
                .vehicleId("vehicle789")
                .projectType("SUSPENSION_UPGRADE")
                .description("Suspension upgrade")
                .desiredCompletionDate("2025-12-15")
                .budget(new BigDecimal("2500.00"))
                .status(ProjectStatus.IN_PROGRESS)
                .progress(30)
                .build();

        projectRepository.save(testProject);
        projectRepository.save(project2);

        List<Project> allProjects = projectRepository.findAll();

        assertThat(allProjects).hasSize(2);
    }

    @Test
    void testProjectWithAppointment() {
        testProject.setAppointmentId("appointment123");
        Project saved = projectRepository.save(testProject);

        assertThat(saved.getAppointmentId()).isEqualTo("appointment123");
    }
}
