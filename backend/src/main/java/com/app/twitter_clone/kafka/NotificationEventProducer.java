package com.app.twitter_clone.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventProducer {

    public static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationEventProducer(
            KafkaTemplate<String, NotificationEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(NotificationEvent event) {

        if (event == null) {
            return;
        }

        Long recipientId = event.getRecipientId();
        Long senderId = event.getSenderId();

        // Do not notify users about their own actions.
        if (recipientId != null && recipientId.equals(senderId)) {
            return;
        }

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(recipientId),
                event
        );
    }
}