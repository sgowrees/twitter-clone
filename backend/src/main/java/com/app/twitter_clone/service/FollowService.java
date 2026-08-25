package com.app.twitter_clone.service;

import com.app.twitter_clone.dto.follow.GetFollowersRequest;
import com.app.twitter_clone.dto.follow.GetFollowingRequest;
import com.app.twitter_clone.dto.follow.ToggleFollowRequest;
import com.app.twitter_clone.kafka.NotificationEvent;
import com.app.twitter_clone.kafka.NotificationEventProducer;
import com.app.twitter_clone.model.Follow;
import com.app.twitter_clone.model.User;
import com.app.twitter_clone.model.NotificationType;
import com.app.twitter_clone.repository.FollowRepository;
import com.app.twitter_clone.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@Transactional
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationEventProducer notificationEventProducer;

    public FollowService(
            FollowRepository followRepository,
            UserRepository userRepository,
            NotificationEventProducer notificationEventProducer) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.notificationEventProducer = notificationEventProducer;
    }

    public Follow addFollow(ToggleFollowRequest request) {
        Long followerId = request.getFollowerId();
        Long followingId = request.getFollowingId();

        if (followerId == null || followingId == null) {
            throw new RuntimeException("Follower and following IDs are required");
        }

        if (followerId.equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        Optional<User> followerResult = userRepository.findById(followerId);
        Optional<User> followingResult = userRepository.findById(followingId);

        if (followerResult.isEmpty() || followingResult.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setCreatedAt(LocalDateTime.now());
        follow.setFollower(followerResult.get());
        follow.setFollowing(followingResult.get());

        Follow saved = followRepository.save(follow);

        notificationEventProducer.publish(new NotificationEvent(
                followingId, followerId, null, NotificationType.FOLLOW
        ));

        return saved;
    }

    public void removeFollow(ToggleFollowRequest request) {
        Long followerId = request.getFollowerId();
        Long followingId = request.getFollowingId();

        if (followerId == null || followingId == null) {
            throw new RuntimeException("Follower and following IDs are required");
        }

        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("Not following this user");
        }

        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    public boolean toggleFollow(ToggleFollowRequest request) {
        if (followRepository.existsByFollowerIdAndFollowingId(
                request.getFollowerId(), request.getFollowingId())) {
            removeFollow(request);
            return false;
        }
        addFollow(request);
        return true;
    }

    public List<Follow> getFollowers(GetFollowersRequest request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }

        return followRepository.findFollowerIdsByFollowingId(request.getUserId());
    }

    public List<Follow> getFollowing(GetFollowingRequest request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }

        return followRepository.findFollowingIdsByFollowerId(request.getUserId());
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }

        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
}