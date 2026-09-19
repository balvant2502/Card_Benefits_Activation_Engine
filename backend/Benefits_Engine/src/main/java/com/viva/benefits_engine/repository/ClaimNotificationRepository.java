package com.viva.benefits_engine.repository;

import com.viva.benefits_engine.models.ClaimNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimNotificationRepository extends JpaRepository<ClaimNotification, Long> {
    List<ClaimNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
}
