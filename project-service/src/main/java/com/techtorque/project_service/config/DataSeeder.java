package com.techtorque.project_service.config;

import com.techtorque.project_service.entity.*;
import com.techtorque.project_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final ServiceNoteRepository serviceNoteRepository;
    private final ProgressPhotoRepository progressPhotoRepository;
    private final InvoiceRepository invoiceRepository;
    private final QuoteRepository quoteRepository;

    // These UUIDs should match the ones from the Authentication service
    // TODO: Update these to match actual UUIDs from Auth service
    private static final String CUSTOMER_1_ID = "customer-uuid-1";
    private static final String CUSTOMER_2_ID = "customer-uuid-2";
    private static final String EMPLOYEE_1_ID = "employee-uuid-1";
    private static final String EMPLOYEE_2_ID = "employee-uuid-2";
    private static final String ADMIN_ID = "admin-uuid-1";

    @Bean
    @Profile("dev")
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("Starting data seeding for Project Service (dev profile)...");

            // Check if data already exists
            if (serviceRepository.count() > 0) {
                log.info("Data already exists. Skipping seeding.");
                return;
            }

            seedStandardServices();
            seedProjects();
            seedServiceNotes();
            seedProgressPhotos();
            seedInvoices();

            log.info("Data seeding completed successfully!");
        };
    }

    private void seedStandardServices() {
        log.info("Seeding standard services...");

        // Service 1: Completed oil change for customer 1
        Set<String> employees1 = new HashSet<>();
        employees1.add(EMPLOYEE_1_ID);

        StandardService service1 = StandardService.builder()
                .appointmentId("APT-001")
                .customerId(CUSTOMER_1_ID)
                .assignedEmployeeIds(employees1)
                .status(ServiceStatus.COMPLETED)
                .progress(100)
                .hoursLogged(2.5)
                .estimatedCompletion(LocalDateTime.now().minusDays(5))
                .build();

        // Service 2: In progress brake service for customer 1
        StandardService service2 = StandardService.builder()
                .appointmentId("APT-002")
                .customerId(CUSTOMER_1_ID)
                .assignedEmployeeIds(employees1)
                .status(ServiceStatus.IN_PROGRESS)
                .progress(60)
                .hoursLogged(3.0)
                .estimatedCompletion(LocalDateTime.now().plusHours(4))
                .build();

        // Service 3: Created service for customer 2
        Set<String> employees2 = new HashSet<>();
        employees2.add(EMPLOYEE_2_ID);

        StandardService service3 = StandardService.builder()
                .appointmentId("APT-003")
                .customerId(CUSTOMER_2_ID)
                .assignedEmployeeIds(employees2)
                .status(ServiceStatus.CREATED)
                .progress(0)
                .hoursLogged(0)
                .estimatedCompletion(LocalDateTime.now().plusDays(2))
                .build();

        List<StandardService> services = serviceRepository.saveAll(List.of(service1, service2, service3));
        log.info("Seeded {} standard services", services.size());
    }

    private void seedProjects() {
        log.info("Seeding custom projects...");

        // Project 1: Approved custom modification for customer 1
        Project project1 = Project.builder()
                .customerId(CUSTOMER_1_ID)
                .vehicleId("VEH-001")
                .description("Install custom exhaust system and performance tuning")
                .budget(new BigDecimal("5000.00"))
                .status(ProjectStatus.APPROVED)
                .progress(0)
                .build();

        // Project 2: Quoted project for customer 2
        Project project2 = Project.builder()
                .customerId(CUSTOMER_2_ID)
                .vehicleId("VEH-002")
                .description("Full interior leather upholstery replacement")
                .budget(new BigDecimal("3000.00"))
                .status(ProjectStatus.QUOTED)
                .progress(0)
                .build();

        // Project 3: In progress project for customer 1
        Project project3 = Project.builder()
                .customerId(CUSTOMER_1_ID)
                .vehicleId("VEH-001")
                .description("Custom body kit installation and paint job")
                .budget(new BigDecimal("8000.00"))
                .status(ProjectStatus.IN_PROGRESS)
                .progress(45)
                .build();

        List<Project> projects = projectRepository.saveAll(List.of(project1, project2, project3));
        log.info("Seeded {} projects", projects.size());

        // Seed quotes for projects
        seedQuotes(projects);
    }

    private void seedQuotes(List<Project> projects) {
        log.info("Seeding quotes...");

        List<Quote> quotes = new ArrayList<>();

        for (Project project : projects) {
            if (project.getStatus() == ProjectStatus.QUOTED || 
                project.getStatus() == ProjectStatus.APPROVED ||
                project.getStatus() == ProjectStatus.IN_PROGRESS) {
                
                Quote quote = Quote.builder()
                        .projectId(project.getId())
                        .laborCost(project.getBudget().multiply(new BigDecimal("0.6")))
                        .partsCost(project.getBudget().multiply(new BigDecimal("0.4")))
                        .totalCost(project.getBudget())
                        .estimatedDays(14)
                        .breakdown("Labor: 60%, Parts: 40%, Estimated completion: 2 weeks")
                        .submittedBy(EMPLOYEE_1_ID)
                        .build();
                
                quotes.add(quote);
            }
        }

        quoteRepository.saveAll(quotes);
        log.info("Seeded {} quotes", quotes.size());
    }

    private void seedServiceNotes() {
        log.info("Seeding service notes...");

        List<StandardService> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            return;
        }

        List<ServiceNote> notes = new ArrayList<>();

        // Notes for first service (completed)
        String serviceId1 = services.get(0).getId();
        notes.add(ServiceNote.builder()
                .serviceId(serviceId1)
                .employeeId(EMPLOYEE_1_ID)
                .note("Started oil change service. Checked oil levels and condition.")
                .isCustomerVisible(true)
                .build());

        notes.add(ServiceNote.builder()
                .serviceId(serviceId1)
                .employeeId(EMPLOYEE_1_ID)
                .note("Oil and filter changed successfully. All fluid levels checked.")
                .isCustomerVisible(true)
                .build());

        notes.add(ServiceNote.builder()
                .serviceId(serviceId1)
                .employeeId(EMPLOYEE_1_ID)
                .note("Internal note: Used synthetic 5W-30 oil as per spec.")
                .isCustomerVisible(false)
                .build());

        // Notes for second service (in progress)
        if (services.size() > 1) {
            String serviceId2 = services.get(1).getId();
            notes.add(ServiceNote.builder()
                    .serviceId(serviceId2)
                    .employeeId(EMPLOYEE_1_ID)
                    .note("Inspected brake pads and rotors. Front pads need replacement.")
                    .isCustomerVisible(true)
                    .build());

            notes.add(ServiceNote.builder()
                    .serviceId(serviceId2)
                    .employeeId(EMPLOYEE_1_ID)
                    .note("Replaced front brake pads. Testing brakes now.")
                    .isCustomerVisible(true)
                    .build());
        }

        serviceNoteRepository.saveAll(notes);
        log.info("Seeded {} service notes", notes.size());
    }

    private void seedProgressPhotos() {
        log.info("Seeding progress photos...");

        List<StandardService> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            return;
        }

        List<ProgressPhoto> photos = new ArrayList<>();

        // Photos for completed service
        String serviceId1 = services.get(0).getId();
        photos.add(ProgressPhoto.builder()
                .serviceId(serviceId1)
                .photoUrl("/uploads/service-photos/oil-change-before.jpg")
                .description("Before oil change - old oil condition")
                .uploadedBy(EMPLOYEE_1_ID)
                .build());

        photos.add(ProgressPhoto.builder()
                .serviceId(serviceId1)
                .photoUrl("/uploads/service-photos/oil-change-after.jpg")
                .description("After oil change - new filter installed")
                .uploadedBy(EMPLOYEE_1_ID)
                .build());

        // Photos for in-progress service
        if (services.size() > 1) {
            String serviceId2 = services.get(1).getId();
            photos.add(ProgressPhoto.builder()
                    .serviceId(serviceId2)
                    .photoUrl("/uploads/service-photos/brake-inspection.jpg")
                    .description("Brake pad inspection - worn pads")
                    .uploadedBy(EMPLOYEE_1_ID)
                    .build());

            photos.add(ProgressPhoto.builder()
                    .serviceId(serviceId2)
                    .photoUrl("/uploads/service-photos/brake-replacement.jpg")
                    .description("New brake pads installed")
                    .uploadedBy(EMPLOYEE_1_ID)
                    .build());
        }

        progressPhotoRepository.saveAll(photos);
        log.info("Seeded {} progress photos", photos.size());
    }

    private void seedInvoices() {
        log.info("Seeding invoices...");

        List<StandardService> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            return;
        }

        List<Invoice> invoices = new ArrayList<>();

        // Invoice for first completed service
        StandardService completedService = services.stream()
                .filter(s -> s.getStatus() == ServiceStatus.COMPLETED)
                .findFirst()
                .orElse(null);

        if (completedService != null) {
            BigDecimal subtotal = new BigDecimal("150.00");
            BigDecimal taxRate = new BigDecimal("0.15");
            BigDecimal taxAmount = subtotal.multiply(taxRate);
            BigDecimal totalAmount = subtotal.add(taxAmount);

            Invoice invoice = Invoice.builder()
                    .invoiceNumber("INV-" + System.currentTimeMillis())
                    .serviceId(completedService.getId())
                    .customerId(completedService.getCustomerId())
                    .items(new ArrayList<>())
                    .subtotal(subtotal)
                    .taxAmount(taxAmount)
                    .totalAmount(totalAmount)
                    .status(InvoiceStatus.PAID)
                    .paidAt(LocalDateTime.now().minusDays(2))
                    .build();

            // Add invoice items
            InvoiceItem laborItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .description("Oil Change Service - Labor")
                    .quantity(1)
                    .unitPrice(new BigDecimal("50.00"))
                    .amount(new BigDecimal("50.00"))
                    .build();

            InvoiceItem oilItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .description("Synthetic Oil 5W-30 (5 quarts)")
                    .quantity(5)
                    .unitPrice(new BigDecimal("12.00"))
                    .amount(new BigDecimal("60.00"))
                    .build();

            InvoiceItem filterItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .description("Oil Filter")
                    .quantity(1)
                    .unitPrice(new BigDecimal("15.00"))
                    .amount(new BigDecimal("15.00"))
                    .build();

            InvoiceItem inspectionItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .description("Multi-point Inspection")
                    .quantity(1)
                    .unitPrice(new BigDecimal("25.00"))
                    .amount(new BigDecimal("25.00"))
                    .build();

            invoice.getItems().add(laborItem);
            invoice.getItems().add(oilItem);
            invoice.getItems().add(filterItem);
            invoice.getItems().add(inspectionItem);

            invoices.add(invoice);
        }

        invoiceRepository.saveAll(invoices);
        log.info("Seeded {} invoices", invoices.size());
    }
}
