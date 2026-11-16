package com.emergency.alert.service;

import com.emergency.alert.dto.AlertCreateRequest;
import com.emergency.alert.dto.AlertResponse;
import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.AlertSeverity;
import com.emergency.alert.model.AlertStatus;
import com.emergency.alert.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlertServiceTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
    }

    @Test
    @DisplayName("Should broadcast alert, persist in database, and assign valid UUID")
    void testBroadcastAlert() {
        AlertCreateRequest request = new AlertCreateRequest();
        request.setCategory(AlertCategory.FIRE);
        request.setSeverity(AlertSeverity.CRITICAL);
        request.setTitle("Wildfire Threatening Sector 9");
        request.setLocation("Sector 9 Foothills");
        request.setDescription("Severe fire outbreak expanding quickly.");
        request.setInstructions("Evacuate south immediately.");
        request.setDurationMinutes(90);

        AlertResponse response = alertService.broadcastAlert(request, "admin");

        assertNotNull(response.getId());
        assertNotNull(response.getAlertUuid());
        assertTrue(response.getAlertUuid().startsWith("ALERT-FIRE-"));
        assertEquals(AlertCategory.FIRE, response.getCategory());
        assertEquals(AlertSeverity.CRITICAL, response.getSeverity());
        assertEquals(AlertStatus.ACTIVE, response.getStatus());
        assertEquals("admin", response.getBroadcastBy());

        // Verify active list
        List<AlertResponse> active = alertService.getActiveAlerts();
        assertEquals(1, active.size());
        assertEquals("Wildfire Threatening Sector 9", active.get(0).getTitle());
    }

    @Test
    @DisplayName("Should resolve an active alert and record resolution timestamp")
    void testResolveAlert() {
        AlertCreateRequest request = new AlertCreateRequest();
        request.setCategory(AlertCategory.FLOOD);
        request.setSeverity(AlertSeverity.HIGH);
        request.setTitle("River Basin Overflow");
        request.setLocation("Cedar Basin");
        request.setDescription("Water levels rising rapidly.");
        request.setInstructions("Move to high ground.");

        AlertResponse created = alertService.broadcastAlert(request, "admin");
        AlertResponse resolved = alertService.resolveAlert(created.getId());

        assertEquals(AlertStatus.RESOLVED, resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());

        // Should no longer appear in active alerts
        List<AlertResponse> active = alertService.getActiveAlerts();
        assertTrue(active.isEmpty());
    }
}
