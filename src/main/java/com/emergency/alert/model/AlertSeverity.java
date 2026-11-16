package com.emergency.alert.model;

/**
 * Severity levels for emergency alerts.
 */
public enum AlertSeverity {
    CRITICAL,   // Immediate action required / Evacuate
    HIGH,       // Serious hazard / Shelter in place
    MEDIUM,     // Advisory / Take precautions
    LOW         // Informational / Resolved status update
}
