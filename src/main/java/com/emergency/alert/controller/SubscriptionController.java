package com.emergency.alert.controller;

import com.emergency.alert.dto.SubscriptionUpdateRequest;
import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.security.SecurityUtils;
import com.emergency.alert.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ResponseEntity<?> getMySubscriptions() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("Authentication required"));
        Set<AlertCategory> subscriptions = subscriptionService.getUserSubscriptions(username);
        return ResponseEntity.ok(Map.of(
                "username", username,
                "subscriptions", subscriptions
        ));
    }

    @PutMapping
    public ResponseEntity<?> updateMySubscriptions(@Valid @RequestBody SubscriptionUpdateRequest request) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("Authentication required"));
        Set<AlertCategory> updated = subscriptionService.updateSubscriptions(username, request.getCategories());
        return ResponseEntity.ok(Map.of(
                "username", username,
                "subscriptions", updated,
                "message", "Alert preferences updated successfully"
        ));
    }
}
