package com.emergency.alert.service;

import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.Role;
import com.emergency.alert.model.User;
import com.emergency.alert.repository.UserRepository;
import com.emergency.alert.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionServiceTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSubscriptionRepository subscriptionRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                "subtester",
                "subtester@test.com",
                "$2a$10$hashedtestpassword",
                "Subscription Tester",
                "+15551234567",
                Role.ROLE_USER
        );
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should successfully update and retrieve category subscriptions for FIRE and FLOOD")
    void testUpdateAndGetSubscriptions() {
        Set<AlertCategory> newCategories = Set.of(AlertCategory.FIRE, AlertCategory.FLOOD);

        Set<AlertCategory> updated = subscriptionService.updateSubscriptions("subtester", newCategories);
        assertEquals(2, updated.size());
        assertTrue(updated.contains(AlertCategory.FIRE));
        assertTrue(updated.contains(AlertCategory.FLOOD));
        assertFalse(updated.contains(AlertCategory.EARTHQUAKE));

        Set<AlertCategory> fetched = subscriptionService.getUserSubscriptions("subtester");
        assertEquals(2, fetched.size());
        assertTrue(fetched.contains(AlertCategory.FIRE));
        assertTrue(fetched.contains(AlertCategory.FLOOD));
    }

    @Test
    @DisplayName("Should retrieve list of usernames subscribed to a specific category")
    void testGetSubscribedUsernames() {
        subscriptionService.updateSubscriptions("subtester", Set.of(AlertCategory.EARTHQUAKE, AlertCategory.SECURITY));

        var quakeUsers = subscriptionService.getSubscribedUsernames(AlertCategory.EARTHQUAKE);
        assertTrue(quakeUsers.contains("subtester"));

        var fireUsers = subscriptionService.getSubscribedUsernames(AlertCategory.FIRE);
        assertFalse(fireUsers.contains("subtester"));
    }
}
