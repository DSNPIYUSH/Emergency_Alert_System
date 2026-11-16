package com.emergency.alert.repository;

import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByUserId(Long userId);
    List<UserSubscription> findByCategory(AlertCategory category);
    boolean existsByUserIdAndCategory(Long userId, AlertCategory category);
    void deleteByUserIdAndCategory(Long userId, AlertCategory category);
    void deleteByUserId(Long userId);
    long countByCategory(AlertCategory category);
}
