package com.app.twitter_clone.service;

import com.app.twitter_clone.dto.comment.CommentRequest;
import com.app.twitter_clone.dto.comment.DeleteCommentRequest;
import com.app.twitter_clone.dto.comment.GetCommentsForPostRequest;
import com.app.twitter_clone.kafka.NotificationEvent;
import com.app.twitter_clone.kafka.NotificationEventProducer;
import com.app.twitter_clone.model.Comment;
import com.app.twitter_clone.model.Post;
import com.app.twitter_clone.model.User;
import com.app.twitter_clone.model.NotificationType;
import com.app.twitter_clone.repository.CommentRepository;
import com.app.twitter_clone.repository.PostRepository;
import com.app.twitter_clone.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationEventProducer notificationEventProducer;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            NotificationEventProducer notificationEventProducer) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationEventProducer = notificationEventProducer;
    }

    // Creates a comment for a post
    public Comment addComment(CommentRequest request) {

        if (request.getUserId() == null || request.getPostId() == null) {
            throw new RuntimeException("User ID and post ID are required");
        }

        Optional<User> userResult = userRepository.findById(request.getUserId());
        Optional<Post> postResult = postRepository.findById(request.getPostId());

        if (userResult.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        if (postResult.isEmpty()) {
            throw new RuntimeException("Post not found");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("Comment content cannot be empty");
        }

        Post post = postResult.get();

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(userResult.get());
        comment.setPost(post);

        Comment saved = commentRepository.save(comment);

        notificationEventProducer.publish(new NotificationEvent(
                post.getUser().getId(), request.getUserId(),
                request.getPostId(), NotificationType.COMMENT
        ));

        return saved;
    }

    // Gets all comments for a post
    public List<Comment> getCommentsForPost(GetCommentsForPostRequest request) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(request.getPostId());
    }

    // Deletes a comment if it belongs to the user
    public void deleteComment(DeleteCommentRequest request) {

        if (request.getUserId() == null || request.getCommentId() == null) {
            throw new RuntimeException("User ID and comment ID are required");
        }

        Optional<Comment> commentResult = commentRepository.findById(request.getCommentId());

        if (commentResult.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }

        Comment comment = commentResult.get();

        if (!comment.getUser().getId().equals(request.getUserId())) {
            throw new RuntimeException("You cannot delete this comment");
        }

        commentRepository.delete(comment);
    }
}