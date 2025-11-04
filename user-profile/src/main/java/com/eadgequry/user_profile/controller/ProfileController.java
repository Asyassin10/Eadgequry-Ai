package com.eadgequry.user_profile.controller;

import com.eadgequry.user_profile.dto.CreateProfileRequest;
import com.eadgequry.user_profile.dto.ProfileResponse;
import com.eadgequry.user_profile.dto.UpdateProfileRequest;
import com.eadgequry.user_profile.exception.ProfileNotFoundException;
import com.eadgequry.user_profile.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for user profile management
 * Provides CRUD operations for user profiles
 */
@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Create a new user profile
     * Called by Auth Service after user registration
     *
     * POST /profiles
     * Request body: { "userId": 1, "name": "John Doe", "email": "john@example.com" }
     * Response: 201 Created with ProfileResponse
     */
    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody CreateProfileRequest request) {
        try {
            logger.info("Received request to create profile for user ID: {}", request.userId());
            ProfileResponse profile = profileService.createProfile(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation Error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error creating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", "Failed to create profile: " + e.getMessage()
            ));
        }
    }

    /**
     * Get user profile by user_id
     *
     * GET /profiles/{user_id}
     * Response: 200 OK with ProfileResponse or 404 Not Found
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<?> getProfile(@PathVariable("user_id") Long userId) {
        try {
            logger.info("Received request to get profile for user ID: {}", userId);
            ProfileResponse profile = profileService.getProfileByUserId(userId);
            return ResponseEntity.ok(profile);
        } catch (ProfileNotFoundException e) {
            logger.warn("Profile not found for user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Not Found",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error fetching profile for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", "Failed to fetch profile: " + e.getMessage()
            ));
        }
    }

    /**
     * Update user profile
     *
     * PUT /profiles/{user_id}
     * Request body: { "name": "John Smith", "avatarUrl": "https://...", "bio": "..." }
     * Response: 200 OK with updated ProfileResponse
     */
    @PutMapping("/{user_id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable("user_id") Long userId,
            @RequestBody UpdateProfileRequest request) {
        try {
            logger.info("Received request to update profile for user ID: {}", userId);
            ProfileResponse profile = profileService.updateProfile(userId, request);
            return ResponseEntity.ok(profile);
        } catch (ProfileNotFoundException e) {
            logger.warn("Profile not found for user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Not Found",
                "message", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation Error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error updating profile for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", "Failed to update profile: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete user profile
     *
     * DELETE /profiles/{user_id}
     * Response: 200 OK with success message or 404 Not Found
     */
    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteProfile(@PathVariable("user_id") Long userId) {
        try {
            logger.info("Received request to delete profile for user ID: {}", userId);
            profileService.deleteProfile(userId);
            return ResponseEntity.ok(Map.of(
                "message", "Profile deleted successfully",
                "userId", userId
            ));
        } catch (ProfileNotFoundException e) {
            logger.warn("Profile not found for user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Not Found",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error deleting profile for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", "Failed to delete profile: " + e.getMessage()
            ));
        }
    }

    /**
     * Health check endpoint
     *
     * GET /profiles/health
     * Response: 200 OK
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "user-profile"
        ));
    }
}
