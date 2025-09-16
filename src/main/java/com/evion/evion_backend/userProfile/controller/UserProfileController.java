package com.evion.evion_backend.userProfile.controller;

import com.evion.evion_backend.userProfile.dto.UpdateUserProfileRequest;
import com.evion.evion_backend.userProfile.dto.UserProfileResponse;
import com.evion.evion_backend.userProfile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    @GetMapping("/{userId}/profile")
    public UserProfileResponse getProfile(@PathVariable Long userId) {
        return service.getUserProfile(userId);
    }

    @PutMapping("/{userId}/profile")
    public UserProfileResponse updateProfile(@PathVariable Long userId,
            @RequestBody UpdateUserProfileRequest req) {
        return service.updateUserProfile(userId, req);
    }
}
