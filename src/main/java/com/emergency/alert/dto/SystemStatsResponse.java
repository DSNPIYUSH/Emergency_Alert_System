package com.emergency.alert.dto;

import com.emergency.alert.model.AlertCategory;
import java.time.LocalDateTime;
import java.util.Map;

public class SystemStatsResponse {

    private long activeAlerts;
    private long totalAlerts;
    private long totalUsers;
    private Map<AlertCategory, Long> subscriptionsByCategory;
    private LocalDateTime timestamp;
    private String disclaimer;

    public SystemStatsResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public long getActiveAlerts() { return activeAlerts; }
    public void setActiveAlerts(long activeAlerts) { this.activeAlerts = activeAlerts; }

    public long getTotalAlerts() { return totalAlerts; }
    public void setTotalAlerts(long totalAlerts) { this.totalAlerts = totalAlerts; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public Map<AlertCategory, Long> getSubscriptionsByCategory() { return subscriptionsByCategory; }
    public void setSubscriptionsByCategory(Map<AlertCategory, Long> subscriptionsByCategory) { this.subscriptionsByCategory = subscriptionsByCategory; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
