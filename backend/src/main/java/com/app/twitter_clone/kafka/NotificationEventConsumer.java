package com.app.twitter_clone.kafka;

import com.app.twitter_clone.model.Notification;
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
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @KafkaListener(
            topics = NotificationEventProducer.TOPIC,
            groupId = "twitter-clone"
    )
    public void consume(NotificationEvent event) {

        if (event.getRecipientId() == null) {
            return;
        }

        if (event.getSenderId() == null) {
            return;
        }

        if (event.getRecipientId().equals(event.getSenderId())) {
            return;
        }

        var recipient = userRepository.findById(event.getRecipientId())
                .orElseThrow();

        var sender = userRepository.findById(event.getSenderId())
                .orElseThrow();

        Notification notification = new Notification();

        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setType(event.getType());

        notificationRepository.save(notification);
    }
}