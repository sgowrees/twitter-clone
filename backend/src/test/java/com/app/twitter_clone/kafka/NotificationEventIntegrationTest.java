package com.app.twitter_clone.kafka;

import com.app.twitter_clone.model.Notification;
import com.app.twitter_clone.model.NotificationType;
import com.app.twitter_clone.model.User;
import com.app.twitter_clone.repository.NotificationRepository;
import com.app.twitter_clone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {NotificationEventProducer.TOPIC},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=twitter-clone-test",
        "spring.kafka.listener.auto-startup=true"
})
class NotificationEventIntegrationTest {

    @Autowired
    private NotificationEventProducer producer;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private User recipient;
    private User sender;

    @BeforeEach
    void setUp() {

        recipient = new User();
        recipient.setUsername(
                "kafkatest_recipient_" + System.nanoTime()
        );
        recipient.setEmail(
                recipient.getUsername() + "@test.com"
        );
        recipient.setPassword(
                passwordEncoder.encode("password")
        );

        recipient = userRepository.saveAndFlush(recipient);

        sender = new User();
        sender.setUsername(
                "kafkatest_sender_" + System.nanoTime()
        );
        sender.setEmail(
                sender.getUsername() + "@test.com"
        );
        sender.setPassword(
                passwordEncoder.encode("password")
        );

        sender = userRepository.saveAndFlush(sender);

        notificationRepository.deleteAll();
    }

    @Test
    void publishedEvent_isConsumedAndCreatesNotification() {

        waitForKafkaListener();

        NotificationEvent event = new NotificationEvent(
                recipient.getId(),
                sender.getId(),
                null,
                NotificationType.FOLLOW
        );

        producer.publish(event);

        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(250, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {

                    List<Notification> notifications =
                            notificationRepository
                                    .findByRecipientIdOrderByCreatedAtDesc(
                                            recipient.getId()
                                    );

                    assertEquals(1, notifications.size());

                    Notification notification = notifications.get(0);

                    assertEquals(
                            NotificationType.FOLLOW,
                            notification.getType()
                    );

                    assertEquals(
                            sender.getId(),
                            notification.getSender().getId()
                    );

                    assertEquals(
                            recipient.getId(),
                            notification.getRecipient().getId()
                    );
                });
    }

    @Test
    void selfNotification_isNeverPublished() {

        waitForKafkaListener();

        NotificationEvent event = new NotificationEvent(
                recipient.getId(),
                recipient.getId(),
                null,
                NotificationType.LIKE
        );

        producer.publish(event);

        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(250, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {

                    List<Notification> notifications =
                            notificationRepository
                                    .findByRecipientIdOrderByCreatedAtDesc(
                                            recipient.getId()
                                    );

                    assertTrue(
                            notifications.isEmpty(),
                            "Self-notifications should never be created"
                    );
                });
    }

    private void waitForKafkaListener() {

        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {

                    boolean assigned = false;

                    for (MessageListenerContainer container :
                            kafkaListenerEndpointRegistry
                                    .getListenerContainers()) {

                        if (container.isRunning()
                                && container.getAssignedPartitions() != null
                                && !container.getAssignedPartitions().isEmpty()) {

                            assigned = true;
                            break;
                        }
                    }

                    assertTrue(
                            assigned,
                            "Kafka listener has not been assigned a partition yet"
                    );
                });
    }
}