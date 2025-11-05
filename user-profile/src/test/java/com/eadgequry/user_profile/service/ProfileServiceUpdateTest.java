package com.eadgequry.user_profile.service;

import com.eadgequry.user_profile.dto.UpdateProfileRequest;
import com.eadgequry.user_profile.dto.ProfileResponse;
import com.eadgequry.user_profile.model.UserProfile;
import com.eadgequry.user_profile.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Additional tests for ProfileService update method
 * Tests all conditional branches for complete coverage
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceUpdateTest {

    @Mock
    private UserProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testProfile = new UserProfile();
        testProfile.setId(1L);
        testProfile.setUserId(100L);
        testProfile.setName("Original Name");
        testProfile.setAvatarUrl("http://old.com/avatar.jpg");
        testProfile.setBio("Old bio");
        testProfile.setPreferences("{\"theme\":\"dark\"}");
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void updateProfile_OnlyName() {
        UpdateProfileRequest request = new UpdateProfileRequest("New Name", null, null, null);
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, request);

        assertThat(response).isNotNull();
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New Name");
    }

    @Test
    void updateProfile_OnlyAvatarUrl() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, "http://new.com/avatar.jpg", null, null);
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, request);

        assertThat(response).isNotNull();
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        assertThat(captor.getValue().getAvatarUrl()).isEqualTo("http://new.com/avatar.jpg");
    }

    @Test
    void updateProfile_OnlyBio() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, "New bio content", null);
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, request);

        assertThat(response).isNotNull();
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        assertThat(captor.getValue().getBio()).isEqualTo("New bio content");
    }

    @Test
    void updateProfile_OnlyPreferences() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null, "{\"theme\":\"light\"}");
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, request);

        assertThat(response).isNotNull();
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferences()).isEqualTo("{\"theme\":\"light\"}");
    }

    @Test
    void updateProfile_EmptyName_ShouldNotUpdate() {
        UpdateProfileRequest request = new UpdateProfileRequest("", null, null, null);
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, request);

        assertThat(response).isNotNull();
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        // Name should remain unchanged because empty string
        assertThat(captor.getValue().getName()).isEqualTo("Original Name");
    }

    @Test
    void updateProfile_WhitespaceName_ShouldNotUpdate() {
        UpdateProfileRequest request = new UpdateProfileRequest("   ", null, null, null);
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, request);

        assertThat(response).isNotNull();
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        // Name should remain unchanged because whitespace-only
        assertThat(captor.getValue().getName()).isEqualTo("Original Name");
    }

    @Test
    void updateProfile_AllFieldsNull_ShouldNotChangeAnything() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null, null);
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, request);

        assertThat(response).isNotNull();
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        // All fields should remain unchanged
        assertThat(captor.getValue().getName()).isEqualTo("Original Name");
        assertThat(captor.getValue().getAvatarUrl()).isEqualTo("http://old.com/avatar.jpg");
        assertThat(captor.getValue().getBio()).isEqualTo("Old bio");
        assertThat(captor.getValue().getPreferences()).isEqualTo("{\"theme\":\"dark\"}");
    }

    @Test
    void updateProfile_AllFieldsProvided() {
        UpdateProfileRequest request = new UpdateProfileRequest(
            "New Name",
            "http://new.com/avatar.jpg",
            "New bio",
            "{\"theme\":\"light\"}"
        );
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, request);

        assertThat(response).isNotNull();
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        // All fields should be updated
        assertThat(captor.getValue().getName()).isEqualTo("New Name");
        assertThat(captor.getValue().getAvatarUrl()).isEqualTo("http://new.com/avatar.jpg");
        assertThat(captor.getValue().getBio()).isEqualTo("New bio");
        assertThat(captor.getValue().getPreferences()).isEqualTo("{\"theme\":\"light\"}");
    }
}
