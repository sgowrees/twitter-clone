package com.app.twitter_clone.kafka;

import com.app.twitter_clone.model.NotificationType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventProducer {

    public static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationEventProducer(
            KafkaTemplate<String, NotificationEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(NotificationEvent event) {

        if (event.getRecipientId() == null) {
            return;
        }

        if (event.getSenderId() == null) {
            return;
        }

        if (event.getRecipientId().equals(event.getSenderId())) {
            return;
        }

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.getRecipientId()),
                event
        );
    }
}