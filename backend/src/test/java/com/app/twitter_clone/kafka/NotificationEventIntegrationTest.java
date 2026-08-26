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
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
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

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.consumer.auto-offset-reset",
                () -> "earliest"
        );
    }

    private User recipient;
    private User sender;

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setUsername("kafkatest_recipient_" + System.nanoTime());
        recipient.setEmail(recipient.getUsername() + "@test.com");
        recipient.setPassword(passwordEncoder.encode("password"));
        recipient = userRepository.save(recipient);

        sender = new User();
        sender.setUsername("kafkatest_sender_" + System.nanoTime());
        sender.setEmail(sender.getUsername() + "@test.com");
        sender.setPassword(passwordEncoder.encode("password"));
        sender = userRepository.save(sender);

        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() ->
                        kafkaListenerEndpointRegistry
                                .getListenerContainers()
                                .stream()
                                .allMatch(container ->
                                        container.getAssignedPartitions() != null
                                                && !container.getAssignedPartitions().isEmpty()
                                )
                );
    }

    @Test
    void publishedEvent_isConsumedAndCreatesNotification() {
        NotificationEvent event = new NotificationEvent(
                recipient.getId(),
                sender.getId(),
                null,
                NotificationType.FOLLOW
        );

        producer.publish(event);

        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notification> notifications =
                            notificationRepository
                                    .findByRecipientIdOrderByCreatedAtDesc(
                                            recipient.getId()
                                    );

                    assertEquals(1, notifications.size());
                    assertEquals(
                            NotificationType.FOLLOW,
                            notifications.get(0).getType()
                    );
                    assertEquals(
                            sender.getId(),
                            notifications.get(0).getSender().getId()
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
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
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