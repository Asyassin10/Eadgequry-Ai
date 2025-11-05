package com.eadgequry.user_profile.service;

import com.eadgequry.user_profile.dto.CreateProfileRequest;
import com.eadgequry.user_profile.dto.ProfileResponse;
import com.eadgequry.user_profile.dto.UpdateProfileRequest;
import com.eadgequry.user_profile.exception.ProfileNotFoundException;
import com.eadgequry.user_profile.model.UserProfile;
import com.eadgequry.user_profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    private UserProfile testProfile;
    private CreateProfileRequest createRequest;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        testProfile = new UserProfile();
        testProfile.setId(1L);
        testProfile.setUserId(100L);
        testProfile.setName("John Doe");
        testProfile.setAvatarUrl("http://example.com/avatar.jpg");
        testProfile.setBio("Test bio");
        testProfile.setPreferences("{\"theme\":\"dark\"}");
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateProfileRequest(100L, "John Doe");
        updateRequest = new UpdateProfileRequest("Jane Doe", "http://example.com/new-avatar.jpg", "New bio", "{\"theme\":\"light\"}");
    }

    @Test
    void createProfile_Success() {
        when(profileRepository.existsByUserId(100L)).thenReturn(false);
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.createProfile(createRequest);

        assertNotNull(response);
        assertEquals(100L, response.userId());
        assertEquals("John Doe", response.name());
        verify(profileRepository).existsByUserId(100L);
        verify(profileRepository).save(any(UserProfile.class));
    }

    @Test
    void createProfile_AlreadyExists() {
        when(profileRepository.existsByUserId(100L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            profileService.createProfile(createRequest);
        });

        verify(profileRepository).existsByUserId(100L);
        verify(profileRepository, never()).save(any());
    }

    @Test
    void getProfileByUserId_Success() {
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));

        ProfileResponse response = profileService.getProfileByUserId(100L);

        assertNotNull(response);
        assertEquals(100L, response.userId());
        assertEquals("John Doe", response.name());
        verify(profileRepository).findByUserId(100L);
    }

    @Test
    void getProfileByUserId_NotFound() {
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> {
            profileService.getProfileByUserId(100L);
        });

        verify(profileRepository).findByUserId(100L);
    }

    @Test
    void updateProfile_Success() {
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        ProfileResponse response = profileService.updateProfile(100L, updateRequest);

        assertNotNull(response);
        verify(profileRepository).findByUserId(100L);
        verify(profileRepository).save(any(UserProfile.class));
    }

    @Test
    void updateProfile_NotFound() {
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> {
            profileService.updateProfile(100L, updateRequest);
        });

        verify(profileRepository).findByUserId(100L);
        verify(profileRepository, never()).save(any());
    }

    @Test
    void deleteProfile_Success() {
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(testProfile));
        doNothing().when(profileRepository).delete(testProfile);

        assertDoesNotThrow(() -> profileService.deleteProfile(100L));

        verify(profileRepository).findByUserId(100L);
        verify(profileRepository).delete(testProfile);
    }

    @Test
    void deleteProfile_NotFound() {
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> {
            profileService.deleteProfile(100L);
        });

        verify(profileRepository).findByUserId(100L);
        verify(profileRepository, never()).delete(any());
    }
}
