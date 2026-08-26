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
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {NotificationEventProducer.TOPIC},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:0",
                "advertised.listeners=PLAINTEXT://localhost:0"
        }
)
class NotificationEventIntegrationTest {

    @Autowired
    private NotificationEventProducer producer;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers")
        );

        registry.add(
                "spring.kafka.consumer.group-id",
                () -> "notification-test-" + System.nanoTime()
        );

        registry.add(
                "spring.kafka.consumer.auto-offset-reset",
                () -> "earliest"
        );

        registry.add(
                "spring.kafka.consumer.enable-auto-commit",
                () -> "true"
        );
    }

    private User recipient;
    private User sender;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        recipient = new User();
        recipient.setUsername("kafkatest_recipient_" + System.nanoTime());
        recipient.setEmail(recipient.getUsername() + "@test.com");
        recipient.setPassword(passwordEncoder.encode("password"));

        recipient = userRepository.saveAndFlush(recipient);

        sender = new User();
        sender.setUsername("kafkatest_sender_" + System.nanoTime());
        sender.setEmail(sender.getUsername() + "@test.com");
        sender.setPassword(passwordEncoder.encode("password"));

        sender = userRepository.saveAndFlush(sender);
    }

    @Test
    void publishedEvent_isConsumedAndCreatesNotification() {

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertTrue(
                            producer != null,
                            "Kafka producer must be available"
                    );
                });

        NotificationEvent event = new NotificationEvent(
                recipient.getId(),
                sender.getId(),
                null,
                NotificationType.FOLLOW
        );

        producer.publish(event);

        await()
                .atMost(20, TimeUnit.SECONDS)
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

        NotificationEvent event = new NotificationEvent(
                recipient.getId(),
                recipient.getId(),
                null,
                NotificationType.LIKE
        );

        producer.publish(event);

        await()
                .atMost(3, TimeUnit.SECONDS)
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
}