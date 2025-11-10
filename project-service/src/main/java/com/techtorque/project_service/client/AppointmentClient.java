package com.techtorque.project_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Client for inter-service communication with Appointment Service
 */
@Component
@Slf4j
public class AppointmentClient {

  private final RestTemplate restTemplate;
  private final String appointmentServiceUrl;

  public AppointmentClient(
      RestTemplate restTemplate,
      @Value("${services.appointment.url:http://localhost:8083}") String appointmentServiceUrl) {
    this.restTemplate = restTemplate;
    this.appointmentServiceUrl = appointmentServiceUrl;
  }

  /**
   * Cancel an appointment (used when project is rejected)
   */
  public void cancelAppointment(String appointmentId, String adminId) {
    try {
      String url = appointmentServiceUrl + "/api/appointments/" + appointmentId;

      HttpHeaders headers = new HttpHeaders();
      headers.set("X-User-Subject", adminId);
      headers.set("X-User-Roles", "ADMIN");

      HttpEntity<Void> request = new HttpEntity<>(headers);

      restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

      log.info("Successfully cancelled appointment {} via Appointment Service", appointmentId);
    } catch (HttpClientErrorException e) {
      log.error("Failed to cancel appointment {}: {}", appointmentId, e.getMessage());
      // Don't throw - project rejection should still succeed even if appointment cancellation fails
    } catch (Exception e) {
      log.error("Error communicating with Appointment Service: {}", e.getMessage());
    }
  }

  /**
   * Update appointment status to CONFIRMED (used when project is approved)
   */
  public void confirmAppointment(String appointmentId, String adminId) {
    try {
      String url = appointmentServiceUrl + "/api/appointments/" + appointmentId + "/status";

      HttpHeaders headers = new HttpHeaders();
      headers.set("X-User-Subject", adminId);
      headers.set("X-User-Roles", "ADMIN");
      headers.setContentType(MediaType.APPLICATION_JSON);

      String body = "{\"newStatus\":\"CONFIRMED\"}";
      HttpEntity<String> request = new HttpEntity<>(body, headers);

      restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);

      log.info("Successfully confirmed appointment {} via Appointment Service", appointmentId);
    } catch (HttpClientErrorException e) {
      log.error("Failed to confirm appointment {}: {}", appointmentId, e.getMessage());
      // Don't throw - project approval should still succeed even if appointment confirmation fails
    } catch (Exception e) {
      log.error("Error communicating with Appointment Service: {}", e.getMessage());
    }
  }
}
