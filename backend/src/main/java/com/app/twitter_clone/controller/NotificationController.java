package com.app.twitter_clone.controller;

import com.app.twitter_clone.dto.follow.FollowResponse;
import com.app.twitter_clone.dto.notification.NotificationResponse;
import com.app.twitter_clone.mapper.NotificationMapper;
import com.app.twitter_clone.model.Notification;
import com.app.twitter_clone.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(
            NotificationService notificationService,
            NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getNotifications(userId);
        List<NotificationResponse> response = notifications.stream()
                .map(notificationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);   
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("Notifications marked as read");
    }
}