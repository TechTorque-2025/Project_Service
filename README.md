# 🛠️ Service & Project Management Service

## 🚦 Build Status

**main**

[![Build and Test Project Service](https://github.com/TechTorque-2025/Project_Service/actions/workflows/buildtest.yaml/badge.svg)](https://github.com/TechTorque-2025/Project_Service/actions/workflows/buildtest.yaml)

**dev**

[![Build and Test Project Service](https://github.com/TechTorque-2025/Project_Service/actions/workflows/buildtest.yaml/badge.svg?branch=dev)](https://github.com/TechTorque-2025/Project_Service/actions/workflows/buildtest.yaml)

This microservice is the core operational hub, managing the lifecycle of both standard services and custom modification projects.

**Assigned Team:** Randitha, Aditha

### 🎯 Key Responsibilities

- **Standard Services:** Track progress, status, work notes, and photos for jobs originating from appointments.
- **Custom Projects:** Manage modification requests, quote submissions, and the quote approval/rejection process.
- Trigger the invoicing process upon job completion.

### ⚙️ Tech Stack

![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

- **Framework:** Java / Spring Boot
- **Database:** PostgreSQL
- **Security:** Spring Security (consumes JWTs)

### ℹ️ API Information

- **Local Port:** `8084`
- **Swagger UI:** [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)

### 🚀 Running Locally

This service is designed to be run as part of the main `docker-compose` setup from the project's root directory.

```bash
# From the root of the TechTorque-2025 project
docker-compose up --build project-service
```
