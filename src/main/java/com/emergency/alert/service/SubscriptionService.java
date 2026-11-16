package com.emergency.alert.service;

import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.User;
import com.emergency.alert.model.UserSubscription;
import com.emergency.alert.repository.UserRepository;
import com.emergency.alert.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    public SubscriptionService(UserRepository userRepository, UserSubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public Set<AlertCategory> getUserSubscriptions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return subscriptionRepository.findByUserId(user.getId()).stream()
                .map(UserSubscription::getCategory)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Set<AlertCategory> updateSubscriptions(String username, Set<AlertCategory> newCategories) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Remove old subscriptions
        subscriptionRepository.deleteByUserId(user.getId());

        // Save new subscriptions
        if (newCategories != null && !newCategories.isEmpty()) {
            List<UserSubscription> subscriptions = newCategories.stream()
                    .map(category -> new UserSubscription(user, category))
                    .collect(Collectors.toList());
            subscriptionRepository.saveAll(subscriptions);
            return newCategories;
        }

        return Collections.emptySet();
    }

    @Transactional(readOnly = true)
    public List<String> getSubscribedUsernames(AlertCategory category) {
        return subscriptionRepository.findByCategory(category).stream()
                .map(sub -> sub.getUser().getUsername())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<AlertCategory, Long> getSubscriptionCounts() {
        Map<AlertCategory, Long> counts = new EnumMap<>(AlertCategory.class);
        for (AlertCategory cat : AlertCategory.values()) {
            counts.put(cat, subscriptionRepository.countByCategory(cat));
        }
        return counts;
    }
}
