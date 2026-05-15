package com.safesign.backend.domain.user.controller;

import com.safesign.backend.domain.user.service.UserService;
import com.safesign.backend.global.auth.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        userService.deleteMe(userDetails.getUserId(), response);
        return ResponseEntity.noContent().build();
    }
}