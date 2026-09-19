package com.viva.benefits_engine.controller;

import com.viva.benefits_engine.models.ClaimNotification;
import com.viva.benefits_engine.repository.ClaimNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/claim-notifications")
@CrossOrigin(origins = "*")
public class ClaimNotificationController {

    @Autowired
    private ClaimNotificationRepository notificationRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserNotifications(@PathVariable Long userId) {
        List<Map<String, Object>> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> Map.<String, Object>of(
                        "id", notification.getId(),
                        "claimId", notification.getClaim().getId(),
                        "benefitName", notification.getClaim().getBenefit().getName(),
                        "message", notification.getMessage(),
                        "read", notification.isRead(),
                        "createdAt", notification.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.countByUserIdAndReadFalse(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ClaimNotification> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notification.setRead(true);
                    return ResponseEntity.ok(notificationRepository.save(notification));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
