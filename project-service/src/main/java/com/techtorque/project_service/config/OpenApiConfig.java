package com.techtorque.project_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Project & Service Management Service
 * 
 * Access Swagger UI at: http://localhost:8084/swagger-ui/index.html
 * Access API docs JSON at: http://localhost:8084/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechTorque Project & Service Management API")
                        .version("1.0.0")
                        .description(
                            "REST API for project and service management. " +
                            "This service handles service definitions, project tracking, and work assignments.\n\n" +
                            "**Key Features:**\n" +
                            "- Service catalog management\n" +
                            "- Project creation and tracking\n" +
                            "- Work assignment and task management\n" +
                            "- Project status and progress tracking\n" +
                            "- Service-project associations\n\n" +
                            "**Authentication:**\n" +
                            "All endpoints require JWT authentication via the API Gateway. " +
                            "The gateway validates the JWT and injects user context via headers."
                        )
                        .contact(new Contact()
                                .name("TechTorque Development Team")
                                .email("dev@techtorque.com")
                                .url("https://techtorque.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://techtorque.com/license"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Local development server"),
                        new Server()
                                .url("http://localhost:8080/api/v1")
                                .description("Local API Gateway"),
                        new Server()
                                .url("https://api.techtorque.com/v1")
                                .description("Production API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from authentication service (validated by API Gateway)")
                        )
                );
    }
}
