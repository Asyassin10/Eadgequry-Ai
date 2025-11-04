package com.eadgequry.user_profile.service;

import com.eadgequry.user_profile.dto.CreateProfileRequest;
import com.eadgequry.user_profile.dto.ProfileResponse;
import com.eadgequry.user_profile.dto.UpdateProfileRequest;
import com.eadgequry.user_profile.exception.ProfileNotFoundException;
import com.eadgequry.user_profile.model.UserProfile;
import com.eadgequry.user_profile.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user profiles
 * Handles CRUD operations with validation and exception handling
 */
@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final UserProfileRepository profileRepository;

    public ProfileService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Create a new user profile
     * Called by Auth Service after user registration via REST POST
     *
     * @param request CreateProfileRequest containing userId and name
     * @return ProfileResponse with created profile data
     * @throws IllegalArgumentException if validation fails or profile already exists
     */
    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        // Validate request
        request.validate();
        logger.info("Creating profile for user ID: {}", request.userId());

        // Check if profile already exists
        if (profileRepository.existsByUserId(request.userId())) {
            throw new IllegalArgumentException("Profile already exists for user ID: " + request.userId());
        }

        // Create new profile
        UserProfile profile = new UserProfile();
        profile.setUserId(request.userId());
        profile.setName(request.name());

        // Save profile
        UserProfile savedProfile = profileRepository.save(profile);
        logger.info("Profile created successfully for user ID: {}", savedProfile.getUserId());

        return ProfileResponse.fromEntity(savedProfile);
    }

    /**
     * Get user profile by user_id
     *
     * @param userId User ID from Auth Service
     * @return ProfileResponse with profile data
     * @throws ProfileNotFoundException if profile not found
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        logger.info("Fetching profile for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        return ProfileResponse.fromEntity(profile);
    }

    /**
     * Update user profile
     * Allows updating name, avatar_url, bio, and preferences
     *
     * @param userId User ID
     * @param request UpdateProfileRequest with fields to update
     * @return ProfileResponse with updated profile data
     * @throws ProfileNotFoundException if profile not found
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        // Validate request
        request.validate();
        logger.info("Updating profile for user ID: {}", userId);

        // Find existing profile
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        // Update fields if provided
        if (request.name() != null && !request.name().trim().isEmpty()) {
            profile.setName(request.name());
            logger.debug("Updated name for user ID: {}", userId);
        }

        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl());
            logger.debug("Updated avatar for user ID: {}", userId);
        }

        if (request.bio() != null) {
            profile.setBio(request.bio());
            logger.debug("Updated bio for user ID: {}", userId);
        }

        if (request.preferences() != null) {
            profile.setPreferences(request.preferences());
            logger.debug("Updated preferences for user ID: {}", userId);
        }

        // Save updated profile
        UserProfile updatedProfile = profileRepository.save(profile);
        logger.info("Profile updated successfully for user ID: {}", userId);

        return ProfileResponse.fromEntity(updatedProfile);
    }

    /**
     * Delete user profile
     *
     * @param userId User ID
     * @throws ProfileNotFoundException if profile not found
     */
    @Transactional
    public void deleteProfile(Long userId) {
        logger.info("Deleting profile for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        profileRepository.delete(profile);
        logger.info("Profile deleted successfully for user ID: {}", userId);
    }
}
