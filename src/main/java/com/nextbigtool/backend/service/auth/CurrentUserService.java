package com.nextbigtool.backend.service.auth;

import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    private final UserDetailsServiceImpl userDetailsService;

    public CurrentUserService(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public AppUser getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userDetailsService.getUserByEmail(email);
    }
}