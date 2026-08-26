package com.app.twitter_clone.kafka;

import com.app.twitter_clone.model.Notification;
import com.app.twitter_clone.model.NotificationType;
import com.app.twitter_clone.model.User;
import com.app.twitter_clone.repository.NotificationRepository;
import com.app.twitter_clone.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationEventConsumer(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @KafkaListener(
            topics = NotificationEventProducer.TOPIC,
            groupId = "twitter-clone"
    )
    public void consume(NotificationEvent event) {

        User recipient = userRepository
                .findById(event.getRecipientId())
                .orElseThrow();

        User sender = null;

        if (event.getSenderId() != null) {
            sender = userRepository
                    .findById(event.getSenderId())
                    .orElse(null);
        }

        Notification notification = new Notification();

        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setType(event.getType());

        notificationRepository.save(notification);
    }
}