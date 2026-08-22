package com.app.twitter_clone.repository;

import com.app.twitter_clone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Used for login lookup and profile pages
    Optional<User> findByUsername(String username);

    // Used for login lookup by email
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long userId);

    // Used during registration to reject duplicate usernames
    boolean existsByUsername(String username);

    // Used during registration to reject duplicate emails
    boolean existsByEmail(String email);

    // Powers the user search bar: matches partial, case-insensitive text
    // against either the username or the display name
    List<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String username, String displayName);
}