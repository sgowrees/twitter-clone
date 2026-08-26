package com.app.twitter_clone.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventProducer {

    public static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationEventProducer(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(NotificationEvent event) {
        // Skip notifying yourself (e.g. liking or commenting on your own post)
        if (event.getRecipientId().equals(event.getSenderId())) {
            return;
        }

        kafkaTemplate.send(TOPIC, event);
    }
}