package com.safesign.backend.domain.user.entity;

import com.safesign.backend.domain.user.enums.ProviderType;
import com.safesign.backend.domain.user.enums.UserRole;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_user",
                        columnNames = {
                                "provider_type",
                                "provider_user_id"
                        }
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 255)
    private String email;

    // 관리자 로그인용 비밀번호
    @Column(length = 255)
    private String password;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider_type",
            nullable = false,
            length = 20
    )
    private ProviderType providerType;

    @Column(
            name = "provider_user_id",
            nullable = false,
            length = 255
    )
    private String providerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(
            String email,
            String password,
            String name,
            ProviderType providerType,
            String providerUserId,
            UserRole role
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.providerType = providerType;
        this.providerUserId = providerUserId;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.role == null) {
            this.role = UserRole.USER;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void withdraw() {
        String suffix = "_deleted_" + this.userId + "_" + System.currentTimeMillis();

        this.email = "deleted_" + this.userId + "_" + System.currentTimeMillis() + "@deleted.local";
        this.password = null;
        this.name = "탈퇴한 사용자";
        this.providerUserId = this.providerUserId + suffix;
        this.deletedAt = LocalDateTime.now();
    }
}