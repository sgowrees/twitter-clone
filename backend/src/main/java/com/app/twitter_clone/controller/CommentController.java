package com.app.twitter_clone.controller;

import com.app.twitter_clone.dto.comment.CommentRequest;
import com.app.twitter_clone.dto.comment.CommentResponse;
import com.app.twitter_clone.dto.comment.DeleteCommentRequest;
import com.app.twitter_clone.dto.comment.GetCommentsForPostRequest;
import com.app.twitter_clone.mapper.CommentMapper;
import com.app.twitter_clone.model.Comment;
import com.app.twitter_clone.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    public CommentController(
            CommentService commentService,
            CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    // Create a comment
    @PostMapping("/create")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {

        request.setUserId(userId);
        request.setPostId(postId);

        Comment comment = commentService.addComment(request);

        return ResponseEntity.ok(commentMapper.toResponse(comment));
    }

    // Get all comments for a post
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long ignored) {

        GetCommentsForPostRequest request = new GetCommentsForPostRequest();
        request.setPostId(postId);

        List<Comment> comments = commentService.getCommentsForPost(request);

        List<CommentResponse> response = comments.stream()
                .map(commentMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    // Delete a comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {
                
        DeleteCommentRequest request = new DeleteCommentRequest();
        request.setUserId(userId);
        request.setCommentId(commentId);

        commentService.deleteComment(request);

         return ResponseEntity.ok("Account removed successfully");
    }
}