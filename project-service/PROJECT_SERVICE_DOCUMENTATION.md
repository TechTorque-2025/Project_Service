# Project Service Documentation

## Overview

The **Project Service** is a comprehensive microservice for the TechTorque-2025 platform that manages both **standard vehicle services** (routine maintenance from appointments) and **custom vehicle modification projects**. It handles the complete lifecycle from service creation/project request through work tracking, progress updates, invoicing, and completion.

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT Bearer Authentication
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **File Storage**: Local file system (configurable)
- **Build Tool**: Maven

## Service Configuration

**Port**: 8084
**API Documentation**: http://localhost:8084/swagger-ui/index.html
**Base URL**: http://localhost:8084/api

## Core Concepts

### 1. Standard Services vs. Custom Projects

| Aspect | Standard Services | Custom Projects |
|--------|-------------------|-----------------|
| **Origin** | Created from approved appointments | Requested directly by customers |
| **Type** | Routine maintenance (oil change, brake service, etc.) | Vehicle modifications (custom paint, performance upgrades) |
| **Workflow** | Appointment → Service → Work → Complete → Invoice | Request → Quote → Approval → Work → Complete → Invoice |
| **Duration** | Same-day or short duration | Multi-day or weeks |
| **Approval** | Automatic (from appointment) | Requires admin approval + customer acceptance of quote |

### 2. Service Lifecycle

```
Standard Service Flow:
Appointment → Service Created → IN_PROGRESS → Work Performed → COMPLETED → Invoice Generated

Custom Project Flow:
Project Requested → PENDING_REVIEW → Quote Submitted → PENDING_APPROVAL →
Customer Accepts → APPROVED → IN_PROGRESS → COMPLETED → Invoice Generated
```

## Main Features

### Standard Services Management

**Key Capabilities**:
- Create service records from approved appointments
- Track service progress and work hours
- Add work notes and technician comments
- Upload progress photos
- Mark service as complete and generate invoice
- Link service to appointment for full traceability

### Custom Project Management

**Key Capabilities**:
- Customer submits modification request with description and budget
- Admin reviews and submits detailed quote
- Customer accepts or rejects quote
- Track multi-phase project progress
- Upload progress photos at various stages
- Generate final invoice upon completion

### Invoicing System

**Features**:
- Automatic invoice generation upon service/project completion
- Line-item breakdown (labor, parts, materials)
- Support for part-payments (deposit + final)
- Integration with Payment Service
- Invoice status tracking (DRAFT, SENT, PAID)

### File Management

**Capabilities**:
- Upload progress photos (up to 10MB per file)
- Support for multiple file formats (JPEG, PNG, etc.)
- Maximum request size: 50MB
- Photos stored in configurable directory
- Photo retrieval by service/project ID

## API Endpoints

### Standard Services Endpoints (`/services`)

#### Create Service from Appointment
```http
POST /api/services
Authorization: Bearer <token>
Role: EMPLOYEE

Request Body:
{
  "appointmentId": "appt-uuid-123",
  "assignedEmployeeIds": ["emp-001", "emp-002"],
  "estimatedHours": 2.5,
  "notes": "Customer reported brake squeaking"
}

Response: 201 Created
{
  "success": true,
  "message": "Service created successfully",
  "data": {
    "id": "service-uuid",
    "appointmentId": "appt-uuid-123",
    "customerId": "customer-uuid",
    "assignedEmployeeIds": ["emp-001", "emp-002"],
    "status": "IN_PROGRESS",
    "progress": 0,
    "hoursLogged": 0.0,
    "estimatedCompletion": "2025-11-12T16:00:00",
    "createdAt": "2025-11-12T10:00:00",
    "updatedAt": "2025-11-12T10:00:00"
  }
}
```

#### List Services
```http
GET /api/services
Authorization: Bearer <token>
Role: CUSTOMER, EMPLOYEE, ADMIN
Query Params: ?status=IN_PROGRESS (optional)

Response: 200 OK
{
  "success": true,
  "message": "Services retrieved successfully",
  "data": [
    {
      "id": "service-uuid-1",
      "appointmentId": "appt-uuid-1",
      "status": "IN_PROGRESS",
      "progress": 50,
      "hoursLogged": 2.5
    },
    ...
  ]
}
```

**Access Control**:
- Customers: See only their own services
- Employees/Admins: See all services (with optional status filter)

#### Get Service Details
```http
GET /api/services/{serviceId}
Authorization: Bearer <token>
Role: CUSTOMER, EMPLOYEE

Response: 200 OK
{
  "success": true,
  "message": "Service retrieved successfully",
  "data": {
    "id": "service-uuid",
    "appointmentId": "appt-uuid",
    "customerId": "customer-uuid",
    "assignedEmployeeIds": ["emp-001"],
    "status": "IN_PROGRESS",
    "progress": 75,
    "hoursLogged": 3.5,
    "estimatedCompletion": "2025-11-12T16:00:00",
    "createdAt": "2025-11-12T10:00:00",
    "updatedAt": "2025-11-12T14:30:00"
  }
}
```

#### Update Service
```http
PATCH /api/services/{serviceId}
Authorization: Bearer <token>
Role: EMPLOYEE

Request Body:
{
  "status": "IN_PROGRESS",
  "progress": 75,
  "notes": "Replaced brake pads, testing now",
  "estimatedCompletion": "2025-11-12T16:00:00"
}

Response: 200 OK
{
  "success": true,
  "message": "Service updated successfully",
  "data": { ... }
}
```

#### Mark Service Complete
```http
POST /api/services/{serviceId}/complete
Authorization: Bearer <token>
Role: EMPLOYEE

Request Body:
{
  "completionNotes": "Service completed successfully. All parts functioning properly.",
  "laborCost": 150.00,
  "partsCost": 85.50,
  "invoiceItems": [
    {
      "description": "Front brake pads replacement",
      "quantity": 1,
      "unitPrice": 85.50
    },
    {
      "description": "Labor - Brake service",
      "quantity": 2,
      "unitPrice": 75.00
    }
  ]
}

Response: 200 OK
{
  "success": true,
  "message": "Service completed successfully",
  "data": {
    "invoiceId": "invoice-uuid",
    "serviceId": "service-uuid",
    "totalAmount": 235.50,
    "items": [...],
    "status": "DRAFT",
    "createdAt": "2025-11-12T16:00:00"
  }
}
```

#### Get Service Invoice
```http
GET /api/services/{serviceId}/invoice
Authorization: Bearer <token>
Role: CUSTOMER

Response: 200 OK
{
  "success": true,
  "message": "Invoice retrieved successfully",
  "data": {
    "invoiceId": "invoice-uuid",
    "serviceId": "service-uuid",
    "totalAmount": 235.50,
    "status": "SENT",
    "items": [...]
  }
}
```

#### Add Service Note
```http
POST /api/services/{serviceId}/notes
Authorization: Bearer <token>
Role: EMPLOYEE

Request Body:
{
  "note": "Started work on brake system. Front rotors need replacement.",
  "isVisibleToCustomer": true
}

Response: 201 Created
{
  "success": true,
  "message": "Note added successfully",
  "data": {
    "id": "note-uuid",
    "serviceId": "service-uuid",
    "employeeId": "emp-001",
    "note": "Started work on brake system...",
    "isVisibleToCustomer": true,
    "createdAt": "2025-11-12T11:00:00"
  }
}
```

#### Get Service Notes
```http
GET /api/services/{serviceId}/notes
Authorization: Bearer <token>
Role: CUSTOMER, EMPLOYEE

Response: 200 OK
{
  "success": true,
  "message": "Notes retrieved successfully",
  "data": [
    {
      "id": "note-uuid-1",
      "note": "Started brake service",
      "employeeId": "emp-001",
      "createdAt": "2025-11-12T11:00:00"
    },
    ...
  ]
}
```

**Note**: Customers only see notes marked as `isVisibleToCustomer: true`

#### Upload Progress Photos
```http
POST /api/services/{serviceId}/photos
Authorization: Bearer <token>
Role: EMPLOYEE
Content-Type: multipart/form-data

Form Data:
files: [photo1.jpg, photo2.jpg]

Response: 201 Created
{
  "success": true,
  "message": "Photos uploaded successfully",
  "data": [
    {
      "id": "photo-uuid-1",
      "serviceId": "service-uuid",
      "fileName": "photo1.jpg",
      "fileUrl": "/uploads/service-photos/photo1.jpg",
      "uploadedAt": "2025-11-12T12:00:00"
    },
    ...
  ]
}
```

#### Get Progress Photos
```http
GET /api/services/{serviceId}/photos
Authorization: Bearer <token>
Role: CUSTOMER, EMPLOYEE

Response: 200 OK
{
  "success": true,
  "message": "Photos retrieved successfully",
  "data": [
    {
      "id": "photo-uuid-1",
      "fileName": "photo1.jpg",
      "fileUrl": "/uploads/service-photos/photo1.jpg",
      "uploadedAt": "2025-11-12T12:00:00"
    },
    ...
  ]
}
```

### Custom Projects Endpoints (`/projects`)

#### Request New Project
```http
POST /api/projects
Authorization: Bearer <token>
Role: CUSTOMER

Request Body:
{
  "vehicleId": "vehicle-uuid",
  "projectType": "Performance Upgrade",
  "description": "Install turbocharger kit and performance exhaust system",
  "desiredCompletionDate": "2025-12-15",
  "budget": 5000.00
}

Response: 201 Created
{
  "success": true,
  "message": "Project request submitted successfully",
  "data": {
    "id": "project-uuid",
    "customerId": "customer-uuid",
    "vehicleId": "vehicle-uuid",
    "projectType": "Performance Upgrade",
    "description": "Install turbocharger kit...",
    "desiredCompletionDate": "2025-12-15",
    "budget": 5000.00,
    "status": "PENDING_REVIEW",
    "progress": 0,
    "createdAt": "2025-11-12T10:00:00"
  }
}
```

#### List Projects
```http
GET /api/projects
Authorization: Bearer <token>
Role: CUSTOMER, EMPLOYEE, ADMIN

Response: 200 OK
{
  "success": true,
  "message": "Projects retrieved successfully",
  "data": [
    {
      "id": "project-uuid-1",
      "projectType": "Performance Upgrade",
      "status": "IN_PROGRESS",
      "progress": 30,
      "budget": 5000.00
    },
    ...
  ]
}
```

**Access Control**:
- Customers: See only their own projects
- Employees/Admins: See all projects

#### Get Project Details
```http
GET /api/projects/{projectId}
Authorization: Bearer <token>
Role: CUSTOMER, EMPLOYEE, ADMIN

Response: 200 OK
{
  "success": true,
  "message": "Project retrieved successfully",
  "data": {
    "id": "project-uuid",
    "customerId": "customer-uuid",
    "vehicleId": "vehicle-uuid",
    "projectType": "Performance Upgrade",
    "description": "Install turbocharger kit...",
    "desiredCompletionDate": "2025-12-15",
    "budget": 5000.00,
    "status": "APPROVED",
    "progress": 30,
    "createdAt": "2025-11-12T10:00:00",
    "updatedAt": "2025-11-13T09:00:00"
  }
}
```

#### Submit Quote for Project
```http
PUT /api/projects/{projectId}/quote
Authorization: Bearer <token>
Role: EMPLOYEE, ADMIN

Request Body:
{
  "estimatedCost": 4800.00,
  "estimatedDurationDays": 14,
  "description": "Detailed breakdown of turbocharger installation including parts, labor, and tuning",
  "items": [
    {
      "description": "Turbocharger kit",
      "quantity": 1,
      "unitPrice": 2500.00
    },
    {
      "description": "Performance exhaust system",
      "quantity": 1,
      "unitPrice": 1200.00
    },
    {
      "description": "Installation labor",
      "quantity": 16,
      "unitPrice": 75.00
    }
  ]
}

Response: 200 OK
{
  "success": true,
  "message": "Quote submitted successfully",
  "data": {
    "id": "project-uuid",
    "status": "PENDING_APPROVAL",
    ...
  }
}
```

#### Accept Quote
```http
POST /api/projects/{projectId}/accept
Authorization: Bearer <token>
Role: CUSTOMER

Response: 200 OK
{
  "success": true,
  "message": "Quote accepted successfully",
  "data": {
    "id": "project-uuid",
    "status": "APPROVED",
    ...
  }
}
```

#### Reject Quote
```http
POST /api/projects/{projectId}/reject
Authorization: Bearer <token>
Role: CUSTOMER

Request Body:
{
  "reason": "Budget too high, looking for more affordable options"
}

Response: 200 OK
{
  "success": true,
  "message": "Quote rejected successfully",
  "data": {
    "id": "project-uuid",
    "status": "REJECTED",
    ...
  }
}
```

#### Update Project Progress
```http
PUT /api/projects/{projectId}/progress
Authorization: Bearer <token>
Role: EMPLOYEE, ADMIN

Request Body:
{
  "progress": 50,
  "notes": "Turbocharger installed, working on exhaust system",
  "estimatedCompletionDate": "2025-12-10"
}

Response: 200 OK
{
  "success": true,
  "message": "Progress updated successfully",
  "data": {
    "id": "project-uuid",
    "progress": 50,
    ...
  }
}
```

#### Approve Project Request
```http
POST /api/projects/{projectId}/approve
Authorization: Bearer <token>
Role: ADMIN

Response: 200 OK
{
  "success": true,
  "message": "Project approved successfully",
  "data": {
    "id": "project-uuid",
    "status": "PENDING_QUOTE",
    ...
  }
}
```

#### Reject Project Request
```http
POST /api/projects/{projectId}/admin/reject
Authorization: Bearer <token>
Role: ADMIN
Query Params: ?reason=Not feasible with current equipment

Response: 200 OK
{
  "success": true,
  "message": "Project rejected successfully",
  "data": {
    "id": "project-uuid",
    "status": "REJECTED",
    ...
  }
}
```

## Status Enumerations

### ServiceStatus
- `IN_PROGRESS` - Service work is ongoing
- `COMPLETED` - Service finished, invoice generated
- `CANCELLED` - Service cancelled

### ProjectStatus
- `PENDING_REVIEW` - Customer submitted, awaiting admin review
- `REJECTED` - Admin rejected the project request
- `PENDING_QUOTE` - Admin approved, awaiting employee quote
- `PENDING_APPROVAL` - Quote submitted, awaiting customer acceptance
- `APPROVED` - Customer accepted quote, ready for work
- `IN_PROGRESS` - Work is ongoing
- `COMPLETED` - Project finished, invoice generated
- `CANCELLED` - Project cancelled

### InvoiceStatus
- `DRAFT` - Invoice created but not sent
- `SENT` - Invoice sent to customer
- `PAID` - Payment received
- `OVERDUE` - Payment past due date
- `CANCELLED` - Invoice cancelled

## Database Schema

### standard_services
- `id` (UUID, Primary Key)
- `appointment_id` (VARCHAR, UNIQUE, NOT NULL)
- `customer_id` (VARCHAR, NOT NULL)
- `assigned_employee_ids` (SET<String>, Collection Table)
- `status` (VARCHAR(20), NOT NULL)
- `progress` (INTEGER, 0-100)
- `hours_logged` (DOUBLE)
- `estimated_completion` (TIMESTAMP)
- `created_at` (TIMESTAMP, NOT NULL)
- `updated_at` (TIMESTAMP, NOT NULL)

### projects
- `id` (UUID, Primary Key)
- `customer_id` (VARCHAR, NOT NULL)
- `vehicle_id` (VARCHAR, NOT NULL)
- `appointment_id` (VARCHAR, NULLABLE)
- `project_type` (VARCHAR, NOT NULL)
- `description` (TEXT, NOT NULL)
- `desired_completion_date` (VARCHAR)
- `budget` (DECIMAL(10,2))
- `status` (VARCHAR(30), NOT NULL)
- `progress` (INTEGER, 0-100)
- `created_at` (TIMESTAMP, NOT NULL)
- `updated_at` (TIMESTAMP, NOT NULL)

### service_notes
- `id` (UUID, Primary Key)
- `service_id` (UUID, Foreign Key)
- `employee_id` (VARCHAR, NOT NULL)
- `note` (TEXT, NOT NULL)
- `is_visible_to_customer` (BOOLEAN, DEFAULT false)
- `created_at` (TIMESTAMP, NOT NULL)

### progress_photos
- `id` (UUID, Primary Key)
- `service_id` (UUID, Foreign Key, NULLABLE)
- `project_id` (UUID, Foreign Key, NULLABLE)
- `file_name` (VARCHAR, NOT NULL)
- `file_path` (VARCHAR, NOT NULL)
- `uploaded_by` (VARCHAR, NOT NULL)
- `uploaded_at` (TIMESTAMP, NOT NULL)

### invoices
- `id` (UUID, Primary Key)
- `service_id` (UUID, Foreign Key, NULLABLE)
- `project_id` (UUID, Foreign Key, NULLABLE)
- `customer_id` (VARCHAR, NOT NULL)
- `total_amount` (DECIMAL(10,2), NOT NULL)
- `status` (VARCHAR(20), NOT NULL)
- `issue_date` (DATE, NOT NULL)
- `due_date` (DATE, NOT NULL)
- `created_at` (TIMESTAMP, NOT NULL)
- `updated_at` (TIMESTAMP, NOT NULL)

### invoice_items
- `id` (UUID, Primary Key)
- `invoice_id` (UUID, Foreign Key)
- `description` (VARCHAR(500), NOT NULL)
- `quantity` (INTEGER, NOT NULL)
- `unit_price` (DECIMAL(10,2), NOT NULL)
- `total_price` (DECIMAL(10,2), NOT NULL)

### quotes
- `id` (UUID, Primary Key)
- `project_id` (UUID, Foreign Key)
- `estimated_cost` (DECIMAL(10,2), NOT NULL)
- `estimated_duration_days` (INTEGER)
- `description` (TEXT)
- `created_at` (TIMESTAMP, NOT NULL)

## Environment Configuration

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=techtorque_projects
DB_USER=techtorque
DB_PASS=techtorque123
DB_MODE=update

# Profile
SPRING_PROFILE=dev

# File Upload
file.upload-dir=uploads/service-photos

# Inter-service URLs
APPOINTMENT_SERVICE_URL=http://localhost:8083
NOTIFICATION_SERVICE_URL=http://localhost:8088
```

## Security & Authorization

### Authentication
- JWT Bearer token required for all endpoints (except public endpoints)
- Token validated by API Gateway
- User information passed via headers: `X-User-Subject`, `X-User-Roles`

### Authorization Matrix

| Endpoint | CUSTOMER | EMPLOYEE | ADMIN |
|----------|----------|----------|-------|
| Create Service | ❌ | ✅ | ❌ |
| List Services | ✅ (own) | ✅ (all) | ✅ (all) |
| Get Service Details | ✅ (own) | ✅ | ✅ |
| Update Service | ❌ | ✅ | ❌ |
| Complete Service | ❌ | ✅ | ❌ |
| Add Service Note | ❌ | ✅ | ❌ |
| Upload Photos | ❌ | ✅ | ❌ |
| Request Project | ✅ | ❌ | ❌ |
| List Projects | ✅ (own) | ✅ (all) | ✅ (all) |
| Submit Quote | ❌ | ✅ | ✅ |
| Accept/Reject Quote | ✅ (own) | ❌ | ❌ |
| Update Progress | ❌ | ✅ | ✅ |
| Approve/Reject Project | ❌ | ❌ | ✅ |

## Integration Points

### Appointment Service
- **Used For**: Fetching appointment details when creating service
- **Endpoint**: `GET /api/appointments/{appointmentId}`
- **Authentication**: Forward JWT token

### Notification Service
- **Used For**: Sending notifications to customers
- **Events**:
  - Service created
  - Service completed
  - Project quote submitted
  - Project status changes
- **Endpoint**: `POST /api/notifications`

## Error Handling

### Common Errors

| Status Code | Error | Description |
|-------------|-------|-------------|
| 400 | Bad Request | Invalid input data, missing required fields |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | User doesn't have permission for this action |
| 404 | Not Found | Service/Project not found |
| 409 | Conflict | Business rule violation (e.g., service already completed) |
| 413 | Payload Too Large | File upload exceeds size limit |
| 500 | Internal Server Error | Server-side error |

### Error Response Format

```json
{
  "timestamp": "2025-11-12T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Service not found with ID: service-uuid",
  "path": "/api/services/service-uuid"
}
```

## Frequently Asked Questions (Q&A)

### General Questions

**Q1: What's the difference between a service and a project?**

A: **Services** are routine maintenance tasks (oil changes, brake repairs) created from appointments. They're typically same-day work. **Projects** are custom modifications (performance upgrades, custom paint) requested by customers that require quotes, approval, and multi-day work.

**Q2: Can a customer create a service directly?**

A: No, services are created by employees from approved appointments. Customers must first book an appointment, then an employee creates the service record when work begins.

**Q3: What happens when a service is marked complete?**

A: The system automatically:
1. Updates service status to `COMPLETED`
2. Generates an invoice with line items
3. Sends notification to customer
4. Links invoice to the service
5. Updates appointment status (via Appointment Service)

**Q4: Can I update a project after it's approved?**

A: Progress and notes can be updated by employees/admins. However, major changes (budget, scope) require a new quote and customer re-approval.

**Q5: How are invoices linked to services and projects?**

A: Each invoice has either a `serviceId` or `projectId` foreign key (one-to-one relationship). When a service/project is completed, the invoice is automatically generated and linked.

### Service Management

**Q6: Can multiple employees work on the same service?**

A: Yes, the `assignedEmployeeIds` field supports multiple employee IDs. This allows team-based service work.

**Q7: How is service progress tracked?**

A: Progress is a percentage (0-100) manually updated by employees. It's typically updated alongside work notes and photo uploads.

**Q8: What's the purpose of service notes?**

A: Service notes document work progress, issues found, and next steps. Notes can be marked as visible to customers for transparency.

**Q9: Can customers view all service notes?**

A: No, customers only see notes where `isVisibleToCustomer: true`. Internal notes (technical details, parts orders) can be hidden.

**Q10: What file types are supported for progress photos?**

A: Common image formats (JPEG, PNG, GIF, BMP). The service validates file types and enforces a 10MB per-file limit.

### Project Management

**Q11: How does the project approval workflow work?**

A:
1. Customer requests project (status: `PENDING_REVIEW`)
2. Admin reviews and approves/rejects
3. If approved, employee submits quote (status: `PENDING_APPROVAL`)
4. Customer accepts or rejects quote
5. If accepted, work begins (status: `APPROVED` → `IN_PROGRESS`)

**Q12: Can a customer negotiate a quote?**

A: Not directly through the API. If a customer rejects a quote with a reason, the employee can submit a revised quote (manual process).

**Q13: What happens if a customer rejects a quote?**

A: The project status becomes `REJECTED` and no further work proceeds. The customer can submit a new project request with adjusted requirements.

**Q14: Can a project be cancelled after work starts?**

A: Yes, but this requires manual intervention. The system doesn't currently support mid-project cancellation with partial billing (future enhancement).

**Q15: How are project progress updates communicated to customers?**

A: Employees update progress percentage and notes, triggering notifications to customers. Customers can also view progress photos for visual updates.

### Invoicing

**Q16: When is an invoice generated?**

A: Automatically when a service/project is marked as complete by an employee. The invoice includes all line items provided in the completion request.

**Q17: Can invoices be edited after creation?**

A: Not through this service. Invoice management (editing, voiding) is handled by the Payment Service.

**Q18: How do customers pay invoices?**

A: Customers are notified of the invoice and can pay through the Payment Service endpoints (separate microservice).

**Q19: What's the difference between `DRAFT` and `SENT` invoice status?**

A: `DRAFT` invoices are created but not yet sent to the customer. `SENT` invoices have been delivered and are awaiting payment.

**Q20: Can services/projects have multiple invoices?**

A: Currently, one-to-one relationship. For complex scenarios (additional work, revisions), create a new service/project record.

### Technical Questions

**Q21: How are photos stored?**

A: Photos are stored on the local file system in a configurable directory (`uploads/service-photos`). File paths are stored in the database.

**Q22: What's the maximum file upload size?**

A: 10MB per file, 50MB per request (multiple files). Configurable in `application.properties`.

**Q23: How does the service integrate with other microservices?**

A: Via REST API calls using `RestTemplate`. JWT tokens are forwarded for authentication. Services use service discovery (configured URLs).

**Q24: What happens if the Notification Service is down?**

A: Notifications fail silently with error logging. The service/project operation completes successfully. Consider implementing a message queue (RabbitMQ, Kafka) for reliability.

**Q25: Can I run this service standalone?**

A: Yes, but notifications and appointment linking won't work. The core service/project management, invoicing, and file uploads function independently.

---

## Summary

The **TechTorque Project Service** is a production-ready microservice that handles:

### Core Features
- **Standard Services**: Routine maintenance from appointments to completion
- **Custom Projects**: Vehicle modifications with quote/approval workflow
- **Work Tracking**: Progress updates, notes, and photo documentation
- **Invoicing**: Automatic invoice generation with line items
- **File Management**: Progress photo uploads and retrieval

### Key Workflows
1. **Service Lifecycle**: Appointment → Service → Work → Complete → Invoice
2. **Project Lifecycle**: Request → Review → Quote → Approval → Work → Complete → Invoice

### Technical Highlights
- **Framework**: Spring Boot 3.5.6 with PostgreSQL
- **Security**: JWT-based authentication and role-based authorization
- **File Handling**: Multipart file uploads with size limits
- **Integration**: REST API calls to Appointment and Notification services
- **API Documentation**: OpenAPI 3.0 (Swagger) for interactive testing

### Use Cases
- Vehicle service shops managing routine maintenance
- Custom modification shops handling complex projects
- Multi-employee work tracking with customer transparency
- Automated invoicing linked to completed work

**Version**: 0.0.1-SNAPSHOT
**Last Updated**: November 2025
**Maintainer**: TechTorque Development Team
