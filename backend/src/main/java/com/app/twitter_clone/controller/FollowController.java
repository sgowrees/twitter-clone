package com.app.twitter_clone.controller;

import com.app.twitter_clone.dto.follow.*;
import com.app.twitter_clone.mapper.FollowMapper;
import com.app.twitter_clone.model.Follow;
import com.app.twitter_clone.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/follows")
public class FollowController {

    private final FollowService followService;
    private final FollowMapper followMapper;

    public FollowController(FollowService followService, FollowMapper followMapper) {
        this.followService = followService;
        this.followMapper = followMapper;
    }

    // Follow a user
    @PostMapping("/{targetId}")
    public ResponseEntity<FollowResponse> addFollow(
            @PathVariable Long userId,
            @PathVariable Long targetId) {

        ToggleFollowRequest request = new ToggleFollowRequest();
        request.setFollowerId(userId);
        request.setFollowingId(targetId);

        Follow follow = followService.addFollow(request);

        return ResponseEntity.ok(followMapper.toResponse(follow, follow.getFollowing()));
    }

    // Get followers of userId
    @GetMapping("/followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable Long userId) {

        GetFollowersRequest request = new GetFollowersRequest();
        request.setUserId(userId);

        List<Follow> followers = followService.getFollowers(request);

        List<FollowResponse> response = followers.stream()
                .map(follow -> followMapper.toResponse(follow, follow.getFollower()))
                .toList();

        return ResponseEntity.ok(response);
    }

    // Get users userId is following
    @GetMapping("/following")
    public ResponseEntity<List<FollowResponse>> getFollowing(@PathVariable Long userId) {

        GetFollowingRequest request = new GetFollowingRequest();
        request.setUserId(userId);

        List<Follow> following = followService.getFollowing(request);

        List<FollowResponse> response = following.stream()
                .map(follow -> followMapper.toResponse(follow, follow.getFollowing()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{targetId}/status")
    public ResponseEntity<Boolean> getFollowStatus(
            @PathVariable Long userId,
            @PathVariable Long targetId) {
        return ResponseEntity.ok(followService.isFollowing(userId, targetId));
    }

    @PostMapping("/{targetId}/toggle")
    public ResponseEntity<Boolean> toggleFollow(
            @PathVariable Long userId,
            @PathVariable Long targetId) {
        ToggleFollowRequest request = new ToggleFollowRequest();
        request.setFollowerId(userId);
        request.setFollowingId(targetId);
        return ResponseEntity.ok(followService.toggleFollow(request));
    }

    // Unfollow a user
    @DeleteMapping("/{targetId}")
    public ResponseEntity<Void> removeFollow(
            @PathVariable Long userId,
            @PathVariable Long targetId) {

        ToggleFollowRequest request = new ToggleFollowRequest();
        request.setFollowerId(userId);
        request.setFollowingId(targetId);

        followService.removeFollow(request);

        return ResponseEntity.noContent().build();
    }
}