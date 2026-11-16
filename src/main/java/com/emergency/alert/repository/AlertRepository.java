package com.emergency.alert.repository;

import com.emergency.alert.model.Alert;
import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    Optional<Alert> findByAlertUuid(String alertUuid);
    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);
    List<Alert> findByCategoryAndStatusOrderByCreatedAtDesc(AlertCategory category, AlertStatus status);
    List<Alert> findByCategoryInAndStatusOrderByCreatedAtDesc(Collection<AlertCategory> categories, AlertStatus status);
    List<Alert> findAllByOrderByCreatedAtDesc();
    long countByStatus(AlertStatus status);
}
