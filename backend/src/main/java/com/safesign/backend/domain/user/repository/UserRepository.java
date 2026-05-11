package com.safesign.backend.domain.user.repository;

import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.enums.ProviderType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByProviderTypeAndProviderUserId(
            ProviderType providerType,
            String providerUserId
    );

    // 관리자 로그인
    Optional<User> findByEmailAndDeletedAtIsNull(
            String email
    );

    // 회원 목록 조회
    List<User> findByDeletedAtIsNull();

    // 회원 상세 조회
    Optional<User> findByUserIdAndDeletedAtIsNull(
            Long userId
    );


}