package com.safesign.backend.domain.user.repository;

import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.enums.ProviderType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByProviderTypeAndProviderUserId(
            ProviderType providerType,
            String providerUserId
    );

    Optional<User> findByEmail(String email);
}