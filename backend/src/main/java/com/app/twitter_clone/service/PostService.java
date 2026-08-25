package com.app.twitter_clone.service;

import com.app.twitter_clone.dto.post.PostRequest;
import com.app.twitter_clone.model.*;
import com.app.twitter_clone.redis.LikeCountCache;
import com.app.twitter_clone.repository.CommentRepository;
import com.app.twitter_clone.repository.FollowRepository;
import com.app.twitter_clone.repository.LikeRepository;
import com.app.twitter_clone.repository.PostRepository;
import com.app.twitter_clone.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final LikeCountCache likeCountCache;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            FollowRepository followRepository,
            LikeCountCache likeCountCache) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.followRepository = followRepository;
        this.likeCountCache = likeCountCache;
    }

    // Creates a post - find user, validate content, save
    public Post createPost(PostRequest request) {
        Optional<User> userResult = userRepository.findById(request.getUserId());
        if (userResult.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userResult.get();
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("Post content cannot be empty");
        }
        Post post = new Post();
        post.setContent(request.getContent());
        post.setUser(user);
        Post saved = postRepository.save(post);
        return saved;

        // Not publishing a NotificationEvent here, unlike like/comment/follow -
        // a new post has no single recipient. Notifying every follower would be
        // a fan-out job (one event per follower), a different pattern from the
        // 1:1 notifications published below.
    }

    // Finds a post by id, throws if not found
    public Post findById(Long postId) {
        if (postId == null) {
            throw new RuntimeException("Post ID is required");
        }
        Optional<Post> postResult = postRepository.findById(postId);
        if (postResult.isEmpty()) {
            throw new RuntimeException("Post not found");
        }
        return postResult.get();
    }

    // Counts likes on a post - checks Redis first, falls back to Postgres on a cache miss
    public long getLikeCount(Long postId) {
        if (postId == null) {
            throw new RuntimeException("Post ID is required");
        }

        Long cached = likeCountCache.get(postId);
        if (cached != null) {
            return cached;
        }

        long count = likeRepository.countByPostId(postId);
        likeCountCache.set(postId, count);
        return count;
    }

    // Counts comments on a post
    public long getCommentCount(Long postId) {
        if (postId == null) {
            throw new RuntimeException("Post ID is required");
        }
        return commentRepository.countByPostId(postId);
    }

    // Checks if a user has liked a post
    public boolean isLikedByUser(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new RuntimeException("Post ID and User ID are required");
        }
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    // Posts from users this userId follows, newest first
    public List<Post> getFeed(Long userId) {
        List<Follow> follows = followRepository.findFollowingIdsByFollowerId(userId);

        // A user's own posts must appear in their home timeline too. Without
        // this, creating a post succeeds but the new post is invisible unless
        // the user follows themselves.
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        userIds.addAll(follows.stream()
            .map(follow -> follow.getFollowing().getId())
            .filter(id -> !id.equals(userId))
            .toList());

        return postRepository.findByUserIdInOrderByCreatedAtDesc(userIds);
    }

    // A user's own posts, newest first
    public List<Post> getUserPosts(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Shows posts from every user, newest first.
    public List<Post> getExplorePosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    // Deletes a post if it belongs to the requester
    public void deletePost(Long postId, Long requesterId) {
        Post post = findById(postId);
        if (!post.getUser().getId().equals(requesterId)) {
            throw new RuntimeException("You cannot delete this post");
        }
        commentRepository.deleteByPostId(postId);
        likeRepository.deleteByPostId(postId);
        postRepository.delete(post);
        likeCountCache.evict(postId);
    }
}