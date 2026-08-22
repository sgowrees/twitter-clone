package com.app.twitter_clone.repository;

import com.app.twitter_clone.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Used to load a user's notification feed, newest first
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Used to show the unread badge count
    long countByRecipientIdAndReadFalse(Long recipientId);

    // Used by the service layer for "mark all as read": fetch this list,
    // flip read = true on each, then save with repository.saveAll(...)
    List<Notification> findByRecipientIdAndReadFalse(Long recipientId);
}