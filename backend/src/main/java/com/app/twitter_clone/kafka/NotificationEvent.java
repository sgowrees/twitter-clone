package com.app.twitter_clone.kafka;

import com.app.twitter_clone.model.NotificationType;

public class NotificationEvent {

    private Long recipientId;
    private Long senderId;
    private Long postId;
    private NotificationType type;

    public NotificationEvent() {
    }

    public NotificationEvent(
            Long recipientId,
            Long senderId,
            Long postId,
            NotificationType type) {

        this.recipientId = recipientId;
        this.senderId = senderId;
        this.postId = postId;
        this.type = type;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}