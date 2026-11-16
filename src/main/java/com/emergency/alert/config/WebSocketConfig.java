package com.emergency.alert.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Primary STOMP endpoint with SockJS fallback for web browsers
        registry.addEndpoint("/ws-alerts")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Direct native WebSocket endpoint (e.g. mobile apps, IoT sirens, microservices)
        registry.addEndpoint("/ws-alerts-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory message broker prefixes for broadcast topics & private queues
        // /topic/alerts/all
        // /topic/alerts/FIRE, /topic/alerts/FLOOD, /topic/alerts/EARTHQUAKE, /topic/alerts/SECURITY
        // /queue for user-specific directed notifications
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for incoming messages sent from client to server (e.g. @MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations (/user/{username}/queue/notifications)
        registry.setUserDestinationPrefix("/user");
    }
}
