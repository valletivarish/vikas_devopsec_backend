package com.surveyplatform.repository;

import com.surveyplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Repository for User entity providing CRUD operations and custom queries
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by their unique username for authentication
    Optional<User> findByUsername(String username);

    // Find a user by their email address for registration validation
    Optional<User> findByEmail(String email);

    // Check if a username already exists to prevent duplicates
    boolean existsByUsername(String username);

    // Check if an email already exists to prevent duplicates
    boolean existsByEmail(String email);
}
