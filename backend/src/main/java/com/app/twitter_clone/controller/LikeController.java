package com.app.twitter_clone.controller;

import com.app.twitter_clone.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/posts/{postId}/like")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    // No request DTO needed - userId and postId already come from the path,
    // there's no other client-typed content for a like toggle.
    @PostMapping("/toggle")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable Long userId,
            @PathVariable Long postId) {

        boolean liked = likeService.toggleLike(userId, postId);
        return ResponseEntity.ok(liked);
    }
}