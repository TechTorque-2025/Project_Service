package com.techtorque.project_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Client for inter-service communication with Notification Service
 */
@Component
@Slf4j
public class NotificationClient {

  private final RestTemplate restTemplate;
  private final String notificationServiceUrl;

  public NotificationClient(
      RestTemplate restTemplate,
      @Value("${services.notification.url:http://localhost:8088}") String notificationServiceUrl) {
    this.restTemplate = restTemplate;
    this.notificationServiceUrl = notificationServiceUrl;
  }

  /**
   * Send project notification to user
   */
  public void sendProjectNotification(String userId, String type, String title, String message, String projectId) {
    try {
      String url = notificationServiceUrl + "/api/notifications/project";

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      String body = String.format(
          "{\"userId\":\"%s\",\"type\":\"%s\",\"title\":\"%s\",\"message\":\"%s\",\"referenceId\":\"%s\",\"referenceType\":\"PROJECT\"}",
          userId, type, title, message, projectId);

      HttpEntity<String> request = new HttpEntity<>(body, headers);

      restTemplate.postForEntity(url, request, String.class);

      log.info("Successfully sent project notification to user {}", userId);
    } catch (HttpClientErrorException e) {
      log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
      // Don't throw - project operations should still succeed even if notification fails
    } catch (Exception e) {
      log.error("Error communicating with Notification Service: {}", e.getMessage());
    }
  }
}
