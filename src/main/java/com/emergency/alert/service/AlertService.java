package com.emergency.alert.service;

import com.emergency.alert.dto.AlertCreateRequest;
import com.emergency.alert.dto.AlertResponse;
import com.emergency.alert.dto.SystemStatsResponse;
import com.emergency.alert.model.Alert;
import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.AlertStatus;
import com.emergency.alert.repository.AlertRepository;
import com.emergency.alert.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final WebSocketNotificationService webSocketNotificationService;

    @Value("${system.disclaimer:DEMO & EDUCATIONAL ALERT SYSTEM - NOT CONNECTED TO OFFICIAL EMERGENCY SERVICES.}")
    private String systemDisclaimer;

    public AlertService(AlertRepository alertRepository,
                        UserRepository userRepository,
                        SubscriptionService subscriptionService,
                        WebSocketNotificationService webSocketNotificationService) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @Transactional
    public AlertResponse broadcastAlert(AlertCreateRequest request, String broadcastBy) {
        // Generate human-readable unique tracking identifier
        String alertUuid = String.format("ALERT-%s-%04d",
                request.getCategory().name(),
                (int) (Math.random() * 9000) + 1000);

        int duration = (request.getDurationMinutes() != null && request.getDurationMinutes() > 0)
                ? request.getDurationMinutes()
                : 120;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(duration);

        Alert alert = new Alert(
                alertUuid,
                request.getCategory(),
                request.getSeverity(),
                request.getTitle(),
                request.getLocation(),
                request.getDescription(),
                request.getInstructions(),
                broadcastBy,
                expiresAt
        );

        Alert savedAlert = alertRepository.save(alert);
        log.info("Emergency Alert broadcast created: ID={}, UUID={}, Category={}, Severity={}",
                savedAlert.getId(), savedAlert.getAlertUuid(), savedAlert.getCategory(), savedAlert.getSeverity());

        // Real-time WebSocket dispatch
        webSocketNotificationService.broadcast(savedAlert);

        // Targeted dispatch to subscribed users
        List<String> subscribers = subscriptionService.getSubscribedUsernames(savedAlert.getCategory());
        webSocketNotificationService.sendToSubscribedUsers(subscribers, savedAlert);

        return AlertResponse.fromEntity(savedAlert);
    }

    @Transactional
    public AlertResponse resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with ID: " + alertId));

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        Alert updated = alertRepository.save(alert);

        log.info("Emergency Alert resolved: ID={}, UUID={}", updated.getId(), updated.getAlertUuid());
        webSocketNotificationService.broadcast(updated);

        return AlertResponse.fromEntity(updated);
    }

    @Transactional
    public AlertResponse cancelAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with ID: " + alertId));

        alert.setStatus(AlertStatus.CANCELLED);
        alert.setResolvedAt(LocalDateTime.now());
        Alert updated = alertRepository.save(alert);

        log.info("Emergency Alert cancelled: ID={}, UUID={}", updated.getId(), updated.getAlertUuid());
        webSocketNotificationService.broadcast(updated);

        return AlertResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getActiveAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.ACTIVE).stream()
                .map(AlertResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AlertResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsForUser(String username) {
        Set<AlertCategory> subs = subscriptionService.getUserSubscriptions(username);
        if (subs.isEmpty()) {
            return Collections.emptyList();
        }
        return alertRepository.findByCategoryInAndStatusOrderByCreatedAtDesc(subs, AlertStatus.ACTIVE).stream()
                .map(AlertResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsByCategory(AlertCategory category) {
        return alertRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, AlertStatus.ACTIVE).stream()
                .map(AlertResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SystemStatsResponse getSystemStats() {
        SystemStatsResponse stats = new SystemStatsResponse();
        stats.setActiveAlerts(alertRepository.countByStatus(AlertStatus.ACTIVE));
        stats.setTotalAlerts(alertRepository.count());
        stats.setTotalUsers(userRepository.count());
        stats.setSubscriptionsByCategory(subscriptionService.getSubscriptionCounts());
        stats.setDisclaimer(systemDisclaimer);
        return stats;
    }
}
