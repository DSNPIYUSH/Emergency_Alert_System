package com.emergency.alert.service;

import com.emergency.alert.dto.AlertResponse;
import com.emergency.alert.model.Alert;
import com.emergency.alert.model.AlertCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts an alert to all listening channels:
     * 1. Global channel: /topic/alerts/all
     * 2. Category channel: /topic/alerts/{CATEGORY} (e.g., /topic/alerts/FIRE)
     */
    public void broadcast(Alert alert) {
        AlertResponse response = AlertResponse.fromEntity(alert);

        // 1. Broadcast to system-wide channel
        String globalDestination = "/topic/alerts/all";
        messagingTemplate.convertAndSend(globalDestination, response);
        log.info("WebSocket: Dispatched alert [{}] to {}", alert.getAlertUuid(), globalDestination);

        // 2. Broadcast to specific category channel (FIRE, FLOOD, EARTHQUAKE, SECURITY)
        String categoryDestination = "/topic/alerts/" + alert.getCategory().name();
        messagingTemplate.convertAndSend(categoryDestination, response);
        log.info("WebSocket: Dispatched alert [{}] to category channel {}", alert.getAlertUuid(), categoryDestination);
    }

    /**
     * Sends private direct notification to a specific subscribed user's queue.
     */
    public void sendToUser(String username, Alert alert) {
        AlertResponse response = AlertResponse.fromEntity(alert);
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", response);
        log.debug("WebSocket: Dispatched user notification [{}] to user [{}]", alert.getAlertUuid(), username);
    }

    /**
     * Dispatches alert to a list of subscribed usernames.
     */
    public void sendToSubscribedUsers(List<String> usernames, Alert alert) {
        for (String username : usernames) {
            sendToUser(username, alert);
        }
    }
}
