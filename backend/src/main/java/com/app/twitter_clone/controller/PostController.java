package com.app.twitter_clone.controller;

import com.app.twitter_clone.dto.post.PostRequest;
import com.app.twitter_clone.dto.post.PostResponse;
import com.app.twitter_clone.mapper.PostMapper;
import com.app.twitter_clone.model.Post;
import com.app.twitter_clone.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/posts")
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    public PostController(
            PostService postService,
            PostMapper postMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
    }

    // Create a post - PostRequest carries content, userId set from path
    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable Long userId,
            @Valid @RequestBody PostRequest request) {

        request.setUserId(userId);

        Post post = postService.createPost(request);
        return ResponseEntity.ok(postMapper.toResponse(post, 0, 0, false));
    }

    // Get a single post
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long userId,
            @PathVariable Long postId) {

        Post post = postService.findById(postId);
        long likeCount = postService.getLikeCount(postId);
        long commentCount = postService.getCommentCount(postId);
        boolean isLiked = postService.isLikedByUser(postId, userId);
        return ResponseEntity.ok(postMapper.toResponse(post, likeCount, commentCount, isLiked));
    }

    // Get the home feed (posts from users userId follows)
    @GetMapping("/feed")
    public ResponseEntity<List<PostResponse>> getFeed(@PathVariable Long userId) {
        List<PostResponse> response = postService.getExplorePosts().stream()
            .map(post -> postMapper.toResponse(
                post,
                postService.getLikeCount(post.getId()),
                postService.getCommentCount(post.getId()),
                postService.isLikedByUser(post.getId(), userId)))
            .toList();
        return ResponseEntity.ok(response);
    }

    // Get userId's own posts
    @GetMapping
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable Long userId) {
        List<PostResponse> response = postService.getUserPosts(userId).stream()
            .map(post -> postMapper.toResponse(
                post,
                postService.getLikeCount(post.getId()),
                postService.getCommentCount(post.getId()),
                postService.isLikedByUser(post.getId(), userId)))
            .toList();
        return ResponseEntity.ok(response);
    }

    // Delete a post
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable Long userId,
            @PathVariable Long postId) {

        postService.deletePost(postId, userId);
        return ResponseEntity.ok("Post removed successfully");
    }
}