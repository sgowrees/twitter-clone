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

// @EmbeddedKafka starts a real, in-memory Kafka broker just for this test
// class - no Docker container needed, no localhost:9092 networking to get
// wrong. It's torn down automatically when the test class finishes.
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { NotificationEventProducer.TOPIC })
class NotificationEventIntegrationTest {

    @Autowired
    private NotificationEventProducer producer;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Redirects the app's Kafka connection settings to the embedded broker
    // instead of the real localhost:9092 / docker "kafka:29092" - this is
    // what makes the test self-contained.
    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> System.getProperty("spring.embedded.kafka.brokers"));
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
    }

    @Test
    void publishedEvent_isConsumedAndCreatesNotification() {
        NotificationEvent event = new NotificationEvent(
                recipient.getId(), sender.getId(), null, NotificationType.FOLLOW
        );

        producer.publish(event);

        // The consumer runs on a separate thread asynchronously, so we poll
        // instead of asserting immediately - this waits up to 10s, checking
        // every 200ms, until the Notification row actually shows up.
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notification> notifications =
                            notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId());

                    assertEquals(1, notifications.size());
                    assertEquals(NotificationType.FOLLOW, notifications.get(0).getType());
                    assertEquals(sender.getId(), notifications.get(0).getSender().getId());
                });
    }

    @Test
    void selfNotification_isNeverPublished() {
        // recipientId == senderId - NotificationEventProducer should skip this
        NotificationEvent event = new NotificationEvent(
                recipient.getId(), recipient.getId(), null, NotificationType.LIKE
        );

        producer.publish(event);

        // Give the consumer a moment to (not) process anything
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        List<Notification> notifications =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId());

        assertTrue(notifications.isEmpty(), "Self-notifications should never be created");
    }
}