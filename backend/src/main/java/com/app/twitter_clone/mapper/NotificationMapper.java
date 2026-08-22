package com.app.twitter_clone.mapper;

import com.app.twitter_clone.dto.notification.NotificationResponse;
import com.app.twitter_clone.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    private final UserMapper userMapper;

    public NotificationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setRead(notification.isRead());
        response.setSender(userMapper.toResponse(notification.getSender()));
        response.setPostId(notification.getPost() != null ? notification.getPost().getId() : null);
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}