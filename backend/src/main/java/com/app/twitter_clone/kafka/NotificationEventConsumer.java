package com.app.twitter_clone.kafka;

import com.app.twitter_clone.dto.notification.NotificationResponse;
import com.app.twitter_clone.mapper.NotificationMapper;
import com.app.twitter_clone.model.Notification;
import com.app.twitter_clone.service.NotificationService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationEventConsumer(
            NotificationService notificationService,
            NotificationMapper notificationMapper,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = NotificationEventProducer.TOPIC,
            groupId = "twitter-clone"
    )
    public void handle(NotificationEvent event) {

        if (event == null) {
            return;
        }

        if (event.getRecipientId() == null) {
            return;
        }

        Notification notification = notificationService.createNotification(
                event.getRecipientId(),
                event.getSenderId(),
                event.getPostId(),
                event.getType()
        );

        NotificationResponse response =
                notificationMapper.toResponse(notification);

        /*
         * Send the notification to the user's WebSocket destination.
         *
         * Example:
         * /topic/notifications/5
         *
         * A client subscribed to that destination receives the
         * notification immediately.
         */
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + event.getRecipientId(),
                response
        );
    }
}