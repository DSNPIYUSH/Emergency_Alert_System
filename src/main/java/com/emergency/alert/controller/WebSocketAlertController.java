package com.emergency.alert.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class WebSocketAlertController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAlertController.class);

    @MessageMapping("/ack")
    @SendToUser("/queue/ack-response")
    public Map<String, Object> handleAcknowledgment(@Payload Map<String, Object> payload, Principal principal) {
        String username = principal != null ? principal.getName() : "anonymous";
        Object alertUuid = payload.get("alertUuid");
        log.info("WebSocket: Received alert acknowledgment from user [{}] for alert [{}]", username, alertUuid);

        return Map.of(
                "status", "ACKNOWLEDGED",
                "alertUuid", alertUuid != null ? alertUuid : "UNKNOWN",
                "user", username,
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public Map<String, Object> handlePing() {
        return Map.of(
                "status", "PONG",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
