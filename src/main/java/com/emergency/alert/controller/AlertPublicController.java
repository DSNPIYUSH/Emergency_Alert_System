package com.emergency.alert.controller;

import com.emergency.alert.dto.AlertResponse;
import com.emergency.alert.dto.SystemStatsResponse;
import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.security.SecurityUtils;
import com.emergency.alert.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AlertPublicController {

    private final AlertService alertService;

    public AlertPublicController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/public/alerts")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping("/public/alerts/category/{category}")
    public ResponseEntity<?> getAlertsByCategory(@PathVariable String category) {
        if (!AlertCategory.isValid(category)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category: " + category));
        }
        AlertCategory cat = AlertCategory.valueOf(category.toUpperCase());
        return ResponseEntity.ok(alertService.getAlertsByCategory(cat));
    }

    @GetMapping("/public/stats")
    public ResponseEntity<SystemStatsResponse> getPublicStats() {
        return ResponseEntity.ok(alertService.getSystemStats());
    }

    @GetMapping("/user/alerts")
    public ResponseEntity<List<AlertResponse>> getUserSubscribedAlerts() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("Authentication required"));
        return ResponseEntity.ok(alertService.getAlertsForUser(username));
    }
}
