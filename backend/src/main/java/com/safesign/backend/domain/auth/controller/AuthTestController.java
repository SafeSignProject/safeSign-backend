package com.safesign.backend.domain.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    @GetMapping("/login/success")
    public String loginSuccess() {
        return "OAuth2 login success";
    }
}