package com.safesign.backend.domain.admin.service;

import com.safesign.backend.domain.admin.dto.response.AdminUserResponse;
import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public List<AdminUserResponse> getUsers() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new AdminUserResponse(
                        user.getUserId(),
                        user.getEmail(),
                        user.getName(),
                        user.getProviderType().name(),
                        user.getRole().name(),
                        user.getCreatedAt()
                ))
                .toList();
    }
}