package com.emergency.alert.dto;

import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.AlertSeverity;
import jakarta.validation.constraints.*;

public class AlertCreateRequest {

    @NotNull(message = "Category is required (FIRE, FLOOD, EARTHQUAKE, SECURITY)")
    private AlertCategory category;

    @NotNull(message = "Severity is required (CRITICAL, HIGH, MEDIUM, LOW)")
    private AlertSeverity severity;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    private String location;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotBlank(message = "Instructions are required")
    @Size(min = 5, max = 2000, message = "Instructions must be between 5 and 2000 characters")
    private String instructions;

    @Min(value = 5, message = "Expiration must be at least 5 minutes")
    @Max(value = 10080, message = "Expiration cannot exceed 7 days (10080 minutes)")
    private Integer durationMinutes = 120; // Default 2 hours

    public AlertCreateRequest() {}

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

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
