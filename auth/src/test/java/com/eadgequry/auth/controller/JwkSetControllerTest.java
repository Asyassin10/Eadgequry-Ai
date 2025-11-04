package com.eadgequry.auth.controller;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwkSetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void jwks_ReturnsJWKSet() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].e").exists())
                .andExpect(jsonPath("$.keys[0].n").exists())
                .andExpect(jsonPath("$.keys[0].kid").exists());
    }

    @Test
    void jwks_ContainsRequiredFields() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").exists())
                .andExpect(jsonPath("$.keys[0].kty").exists())
                .andExpect(jsonPath("$.keys[0].e").exists())
                .andExpect(jsonPath("$.keys[0].n").exists())
                .andExpect(jsonPath("$.keys[0].kid").exists());
    }
}
