# 🛠️ Service & Project Management Service

This microservice is the core operational hub, managing the lifecycle of both standard services and custom modification projects.

**Assigned Team:** Randitha, Aditha

### 🎯 Key Responsibilities

-   **Standard Services:** Track progress, status, work notes, and photos for jobs originating from appointments.
-   **Custom Projects:** Manage modification requests, quote submissions, and the quote approval/rejection process.
-   Trigger the invoicing process upon job completion.

### ⚙️ Tech Stack

-   **Framework:** Java / Spring Boot
-   **Database:** PostgreSQL
-   **Security:** Spring Security (consumes JWTs)

### ℹ️ API Information

-   **Local Port:** `8084`
-   **Swagger UI:** [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)

### 🚀 Running Locally

This service is designed to be run as part of the main `docker-compose` setup from the project's root directory.

```bash
# From the root of the TechTorque-2025 project
docker-compose up --build project-service