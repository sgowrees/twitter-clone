package com.app.twitter_clone.service;

import com.app.twitter_clone.repository.NotificationRepository;
import com.app.twitter_clone.repository.PostRepository;
import com.app.twitter_clone.repository.UserRepository;
import com.app.twitter_clone.model.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, PostRepository postRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // Called by the Kafka consumer (like/comment/follow events), not by a controller directly
    public Notification createNotification(Long recipientId, Long senderId, Long postId, NotificationType type) {
        Optional<User> recipientResult = userRepository.findById(recipientId);
        Optional<User> senderResult = userRepository.findById(senderId);

        if (recipientResult.isEmpty() || senderResult.isEmpty()){
            throw new RuntimeException("sender or recipient is wrong");
        }
        User recipient = recipientResult.get();
        User sender = senderResult.get();

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setRead(false);

        if (postId != null) {
            Optional<Post> postResult = postRepository.findById(postId);
            if (postResult.isEmpty()) {
                throw new RuntimeException("post not found");
            }
            notification.setPost(postResult.get());
        }

        Notification saved = notificationRepository.save(notification);
 
        // TODO: push over WebSocket to the recipient if they're online
 
        return saved;



    }

    // A user's notifications, newest first
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    // Count of unread notifications for the badge
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    // Marks all of a user's unread notifications as read
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFalse(userId);

        unread.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(unread);
    }
}