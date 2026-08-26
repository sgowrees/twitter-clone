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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class NotificationEventIntegrationTest {

    @Autowired
    private NotificationEventProducer producer;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User recipient;
    private User sender;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

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
                recipient.getId(),
                sender.getId(),
                null,
                NotificationType.FOLLOW
        );

        producer.publish(event);

        await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
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

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<Notification> notifications =
                notificationRepository
                        .findByRecipientIdOrderByCreatedAtDesc(
                                recipient.getId()
                        );

        assertTrue(notifications.isEmpty());
    }
}