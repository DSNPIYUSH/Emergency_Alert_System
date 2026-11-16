package com.emergency.alert.dto;

import com.emergency.alert.model.Alert;
import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.AlertSeverity;
import com.emergency.alert.model.AlertStatus;
import java.time.LocalDateTime;

public class AlertResponse {

    private Long id;
    private String alertUuid;
    private AlertCategory category;
    private AlertSeverity severity;
    private String title;
    private String location;
    private String description;
    private String instructions;
    private AlertStatus status;
    private String broadcastBy;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime resolvedAt;

    public AlertResponse() {}

    public static AlertResponse fromEntity(Alert alert) {
        AlertResponse resp = new AlertResponse();
        resp.setId(alert.getId());
        resp.setAlertUuid(alert.getAlertUuid());
        resp.setCategory(alert.getCategory());
        resp.setSeverity(alert.getSeverity());
        resp.setTitle(alert.getTitle());
        resp.setLocation(alert.getLocation());
        resp.setDescription(alert.getDescription());
        resp.setInstructions(alert.getInstructions());
        resp.setStatus(alert.getStatus());
        resp.setBroadcastBy(alert.getBroadcastBy());
        resp.setCreatedAt(alert.getCreatedAt());
        resp.setExpiresAt(alert.getExpiresAt());
        resp.setResolvedAt(alert.getResolvedAt());
        return resp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAlertUuid() { return alertUuid; }
    public void setAlertUuid(String alertUuid) { this.alertUuid = alertUuid; }

    public AlertCategory getCategory() { return category; }
    public void setCategory(AlertCategory category) { this.category = category; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public String getBroadcastBy() { return broadcastBy; }
    public void setBroadcastBy(String broadcastBy) { this.broadcastBy = broadcastBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
