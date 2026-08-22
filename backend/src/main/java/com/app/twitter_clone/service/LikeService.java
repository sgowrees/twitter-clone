package com.app.twitter_clone.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.twitter_clone.model.Like;
import com.app.twitter_clone.model.Post;
import com.app.twitter_clone.model.User;
import com.app.twitter_clone.model.NotificationType;
import com.app.twitter_clone.repository.LikeRepository;
import com.app.twitter_clone.repository.PostRepository;
import com.app.twitter_clone.repository.UserRepository;

@Service
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LikeService(
            LikeRepository likeRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // Toggle: unlike if already liked, like if not. Returns new liked state.
    public boolean toggleLike(Long userId, Long postId) {
        if (userId == null || postId == null) {
            throw new RuntimeException("User ID and post ID are required");
        }
        Optional<User> userResult = userRepository.findById(userId);
        Optional<Post> postResult = postRepository.findById(postId);

        if (userResult.isEmpty() || postResult.isEmpty()){
            throw new RuntimeException("user or post not found");
        }
                boolean alreadyLiked = likeRepository.existsByUserIdAndPostId(userId, postId);
 
        if (alreadyLiked) {
            likeRepository.deleteByUserIdAndPostId(userId, postId);
            return false;
        }
        User user = userResult.get();
        Post post = postResult.get();
        Like like = new Like();
        like.setUser(user);
        like.setPost(post);

        likeRepository.save(like);

        if (!post.getUser().getId().equals(userId)) {
            notificationService.createNotification(
                post.getUser().getId(), userId, postId,
                NotificationType.LIKE);
        }

        // TODO: publish Kafka event e.g. "post.liked" for the notification service
 
        return true;

 


    }
}