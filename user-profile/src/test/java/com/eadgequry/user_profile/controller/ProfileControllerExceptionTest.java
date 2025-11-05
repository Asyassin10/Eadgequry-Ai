package com.eadgequry.user_profile.controller;

import com.eadgequry.user_profile.dto.CreateProfileRequest;
import com.eadgequry.user_profile.dto.UpdateProfileRequest;
import com.eadgequry.user_profile.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Additional tests for ProfileController exception handling
 * Tests all Exception catch blocks to achieve full coverage
 */
@WebMvcTest(ProfileController.class)
class ProfileControllerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @Test
    void createProfile_GenericException() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(100L, "John Doe");

        when(profileService.createProfile(any(CreateProfileRequest.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Failed to create profile: Database connection error"));
    }

    @Test
    void getProfile_GenericException() throws Exception {
        when(profileService.getProfileByUserId(100L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/profiles/100"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Failed to fetch profile: Database error"));
    }

    @Test
    void updateProfile_IllegalArgumentException() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("", null, null, null);

        when(profileService.updateProfile(eq(100L), any(UpdateProfileRequest.class)))
                .thenThrow(new IllegalArgumentException("Name cannot be empty"));

        mockMvc.perform(put("/profiles/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Name cannot be empty"));
    }

    @Test
    void updateProfile_GenericException() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("John", null, null, null);

        when(profileService.updateProfile(eq(100L), any(UpdateProfileRequest.class)))
                .thenThrow(new RuntimeException("Transaction rollback error"));

        mockMvc.perform(put("/profiles/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Failed to update profile: Transaction rollback error"));
    }

    @Test
    void deleteProfile_GenericException() throws Exception {
        when(profileService.deleteProfile(100L))
                .thenThrow(new RuntimeException("Constraint violation"));

        mockMvc.perform(delete("/profiles/100"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Failed to delete profile: Constraint violation"));
    }
}
