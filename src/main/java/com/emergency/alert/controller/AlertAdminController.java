package com.emergency.alert.controller;

import com.emergency.alert.dto.AlertCreateRequest;
import com.emergency.alert.dto.AlertResponse;
import com.emergency.alert.dto.SystemStatsResponse;
import com.emergency.alert.security.SecurityUtils;
import com.emergency.alert.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AlertAdminController {

    private final AlertService alertService;

    public AlertAdminController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/alerts")
    public ResponseEntity<?> broadcastAlert(@Valid @RequestBody AlertCreateRequest request) {
        String adminUsername = SecurityUtils.getCurrentUsername().orElse("admin");
        try {
            AlertResponse response = alertService.broadcastAlert(request, adminUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/alerts/{id}/resolve")
    public ResponseEntity<?> resolveAlert(@PathVariable Long id) {
        try {
            AlertResponse response = alertService.resolveAlert(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/alerts/{id}/cancel")
    public ResponseEntity<?> cancelAlert(@PathVariable Long id) {
        try {
            AlertResponse response = alertService.cancelAlert(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertResponse>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/stats")
    public ResponseEntity<SystemStatsResponse> getSystemStats() {
        return ResponseEntity.ok(alertService.getSystemStats());
    }
}
