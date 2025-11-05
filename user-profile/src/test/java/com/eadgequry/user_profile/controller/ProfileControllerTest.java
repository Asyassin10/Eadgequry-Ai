package com.eadgequry.user_profile.controller;

import com.eadgequry.user_profile.dto.CreateProfileRequest;
import com.eadgequry.user_profile.dto.ProfileResponse;
import com.eadgequry.user_profile.dto.UpdateProfileRequest;
import com.eadgequry.user_profile.exception.ProfileNotFoundException;
import com.eadgequry.user_profile.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    private ProfileResponse profileResponse;
    private CreateProfileRequest createRequest;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        profileResponse = new ProfileResponse(
                1L,
                100L,
                "John Doe",
                "http://example.com/avatar.jpg",
                "Test bio",
                "{\"theme\":\"dark\"}",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        createRequest = new CreateProfileRequest(100L, "John Doe");
        updateRequest = new UpdateProfileRequest("Jane Doe", "http://example.com/avatar.jpg", "New bio", "{\"theme\":\"light\"}");
    }

    @Test
    void createProfile_Success() throws Exception {
        when(profileService.createProfile(any(CreateProfileRequest.class))).thenReturn(profileResponse);

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(profileService).createProfile(any(CreateProfileRequest.class));
    }

    @Test
    void createProfile_ValidationError() throws Exception {
        when(profileService.createProfile(any(CreateProfileRequest.class)))
                .thenThrow(new IllegalArgumentException("Profile already exists"));

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(profileService).createProfile(any(CreateProfileRequest.class));
    }

    @Test
    void getProfile_Success() throws Exception {
        when(profileService.getProfileByUserId(100L)).thenReturn(profileResponse);

        mockMvc.perform(get("/profiles/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(profileService).getProfileByUserId(100L);
    }

    @Test
    void getProfile_NotFound() throws Exception {
        when(profileService.getProfileByUserId(100L))
                .thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(get("/profiles/100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(profileService).getProfileByUserId(100L);
    }

    @Test
    void updateProfile_Success() throws Exception {
        when(profileService.updateProfile(eq(100L), any(UpdateProfileRequest.class)))
                .thenReturn(profileResponse);

        mockMvc.perform(put("/profiles/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(100L));

        verify(profileService).updateProfile(eq(100L), any(UpdateProfileRequest.class));
    }

    @Test
    void updateProfile_NotFound() throws Exception {
        when(profileService.updateProfile(eq(100L), any(UpdateProfileRequest.class)))
                .thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(put("/profiles/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(profileService).updateProfile(eq(100L), any(UpdateProfileRequest.class));
    }

    @Test
    void deleteProfile_Success() throws Exception {
        doNothing().when(profileService).deleteProfile(100L);

        mockMvc.perform(delete("/profiles/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile deleted successfully"))
                .andExpect(jsonPath("$.userId").value(100L));

        verify(profileService).deleteProfile(100L);
    }

    @Test
    void deleteProfile_NotFound() throws Exception {
        doThrow(new ProfileNotFoundException("Profile not found"))
                .when(profileService).deleteProfile(100L);

        mockMvc.perform(delete("/profiles/100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(profileService).deleteProfile(100L);
    }

    @Test
    void health_Check() throws Exception {
        mockMvc.perform(get("/profiles/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("user-profile"));
    }
}
