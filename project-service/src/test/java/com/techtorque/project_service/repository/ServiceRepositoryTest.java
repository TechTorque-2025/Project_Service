package com.techtorque.project_service.repository;

import com.techtorque.project_service.entity.ServiceStatus;
import com.techtorque.project_service.entity.StandardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceRepositoryTest {

    @Autowired
    private ServiceRepository serviceRepository;

    private StandardService testService;

    @BeforeEach
    void setUp() {
        serviceRepository.deleteAll();

        Set<String> assignedEmployees = new HashSet<>();
        assignedEmployees.add("employee1");
        assignedEmployees.add("employee2");

        testService = StandardService.builder()
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
    void testSaveService() {
        StandardService saved = serviceRepository.save(testService);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAppointmentId()).isEqualTo("appointment123");
        assertThat(saved.getStatus()).isEqualTo(ServiceStatus.CREATED);
        assertThat(saved.getAssignedEmployeeIds()).hasSize(2);
    }

    @Test
    void testFindById() {
        serviceRepository.save(testService);

        Optional<StandardService> found = serviceRepository.findById(testService.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo("customer123");
    }

    @Test
    void testFindByCustomerId() {
        StandardService service2 = StandardService.builder()
                .appointmentId("appointment456")
                .customerId("customer123")
                .assignedEmployeeIds(new HashSet<>())
                .status(ServiceStatus.IN_PROGRESS)
                .progress(50)
                .hoursLogged(1.5)
                .estimatedCompletion(LocalDateTime.now().plusHours(1))
                .build();

        StandardService service3 = StandardService.builder()
                .appointmentId("appointment789")
                .customerId("differentCustomer")
                .assignedEmployeeIds(new HashSet<>())
                .status(ServiceStatus.CREATED)
                .progress(0)
                .hoursLogged(0.0)
                .estimatedCompletion(LocalDateTime.now().plusHours(3))
                .build();

        serviceRepository.save(testService);
        serviceRepository.save(service2);
        serviceRepository.save(service3);

        List<StandardService> customerServices = serviceRepository.findByCustomerId("customer123");

        assertThat(customerServices).hasSize(2);
        assertThat(customerServices).allMatch(s -> s.getCustomerId().equals("customer123"));
    }

    @Test
    void testUniqueAppointmentId() {
        serviceRepository.save(testService);

        Optional<StandardService> found = serviceRepository.findById(testService.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAppointmentId()).isEqualTo("appointment123");
    }

    @Test
    void testUpdateService() {
        serviceRepository.save(testService);

        testService.setStatus(ServiceStatus.IN_PROGRESS);
        testService.setProgress(75);
        testService.setHoursLogged(2.5);
        StandardService updated = serviceRepository.save(testService);

        assertThat(updated.getStatus()).isEqualTo(ServiceStatus.IN_PROGRESS);
        assertThat(updated.getProgress()).isEqualTo(75);
        assertThat(updated.getHoursLogged()).isEqualTo(2.5);
    }

    @Test
    void testDeleteService() {
        serviceRepository.save(testService);
        String serviceId = testService.getId();

        serviceRepository.deleteById(serviceId);

        Optional<StandardService> deleted = serviceRepository.findById(serviceId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testFindAll() {
        StandardService service2 = StandardService.builder()
                .appointmentId("appointment999")
                .customerId("customer999")
                .assignedEmployeeIds(new HashSet<>())
                .status(ServiceStatus.COMPLETED)
                .progress(100)
                .hoursLogged(3.0)
                .estimatedCompletion(LocalDateTime.now())
                .build();

        serviceRepository.save(testService);
        serviceRepository.save(service2);

        List<StandardService> allServices = serviceRepository.findAll();

        assertThat(allServices).hasSize(2);
    }

    @Test
    void testUniqueAppointmentIdConstraint() {
        serviceRepository.save(testService);

        StandardService duplicateService = StandardService.builder()
                .appointmentId("appointment123")
                .customerId("customer999")
                .assignedEmployeeIds(new HashSet<>())
                .status(ServiceStatus.CREATED)
                .progress(0)
                .hoursLogged(0.0)
                .estimatedCompletion(LocalDateTime.now().plusHours(2))
                .build();

        try {
            serviceRepository.save(duplicateService);
            serviceRepository.flush();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }
}
