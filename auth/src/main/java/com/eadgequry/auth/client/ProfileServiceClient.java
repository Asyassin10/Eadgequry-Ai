package com.eadgequry.auth.client;

import com.eadgequry.auth.client.dto.CreateProfileRequest;
import com.eadgequry.auth.client.dto.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Profile Service
 * Uses Eureka service discovery to find user-profile service
 */
@FeignClient(name = "user-profile", path = "/profiles")
public interface ProfileServiceClient {

    /**
     * Create a new user profile
     * Called after successful user registration
     *
     * @param request CreateProfileRequest with userId and name
     * @return ProfileResponse with created profile data
     */
    @PostMapping
    ResponseEntity<ProfileResponse> createProfile(@RequestBody CreateProfileRequest request);
}
