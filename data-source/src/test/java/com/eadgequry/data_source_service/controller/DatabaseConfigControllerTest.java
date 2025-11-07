package com.eadgequry.data_source_service.controller;

import com.eadgequry.data_source_service.dto.CreateDatabaseConfigRequest;
import com.eadgequry.data_source_service.dto.DatabaseConfigDTO;
import com.eadgequry.data_source_service.service.DatabaseConfigService;
import com.eadgequry.data_source_service.service.DatabaseConnectionTestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DatabaseConfigController.class)
class DatabaseConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DatabaseConfigService databaseConfigService;

    private DatabaseConfigDTO testConfigDTO;
    private CreateDatabaseConfigRequest testRequest;

    @BeforeEach
    void setUp() {
        testConfigDTO = new DatabaseConfigDTO();
        testConfigDTO.setId(1L);
        testConfigDTO.setUserId(100L);
        testConfigDTO.setName("Test DB");
        testConfigDTO.setType("mysql");
        testConfigDTO.setHost("localhost");
        testConfigDTO.setPort(3306);
        testConfigDTO.setDatabaseName("test_db");
        testConfigDTO.setUsername("testuser");
        testConfigDTO.setStatus("active");
        testConfigDTO.setIsConnected(true);
        testConfigDTO.setCreatedAt(LocalDateTime.now());
        testConfigDTO.setUpdatedAt(LocalDateTime.now());

        testRequest = new CreateDatabaseConfigRequest();
        testRequest.setName("Test DB");
        testRequest.setType("mysql");
        testRequest.setHost("localhost");
        testRequest.setPort(3306);
        testRequest.setDatabaseName("test_db");
        testRequest.setUsername("testuser");
        testRequest.setPassword("testpass");
    }

    @Test
    void getAllConfigs_ShouldReturnListOfConfigs() throws Exception {
        // Arrange
        List<DatabaseConfigDTO> configs = Arrays.asList(testConfigDTO);
        when(databaseConfigService.getAllConfigsByUser(100L)).thenReturn(configs);

        // Act & Assert
        mockMvc.perform(get("/api/datasource/configs/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test DB"))
                .andExpect(jsonPath("$[0].type").value("mysql"));

        verify(databaseConfigService).getAllConfigsByUser(100L);
    }

    @Test
    void getConfigById_ShouldReturnConfig() throws Exception {
        // Arrange
        when(databaseConfigService.getConfigById(1L, 100L)).thenReturn(testConfigDTO);

        // Act & Assert
        mockMvc.perform(get("/api/datasource/configs/1/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test DB"))
                .andExpect(jsonPath("$.type").value("mysql"));

        verify(databaseConfigService).getConfigById(1L, 100L);
    }

    @Test
    void createConfig_ShouldReturnCreatedConfig() throws Exception {
        // Arrange
        when(databaseConfigService.createConfig(eq(100L), any(CreateDatabaseConfigRequest.class)))
                .thenReturn(testConfigDTO);

        // Act & Assert
        mockMvc.perform(post("/api/datasource/configs/user/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test DB"))
                .andExpect(jsonPath("$.type").value("mysql"));

        verify(databaseConfigService).createConfig(eq(100L), any(CreateDatabaseConfigRequest.class));
    }

    @Test
    void updateConfig_ShouldReturnUpdatedConfig() throws Exception {
        // Arrange
        when(databaseConfigService.updateConfig(eq(1L), eq(100L), any(CreateDatabaseConfigRequest.class)))
                .thenReturn(testConfigDTO);

        // Act & Assert
        mockMvc.perform(put("/api/datasource/configs/1/user/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test DB"));

        verify(databaseConfigService).updateConfig(eq(1L), eq(100L), any(CreateDatabaseConfigRequest.class));
    }

    @Test
    void deleteConfig_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(databaseConfigService).deleteConfig(1L, 100L);

        // Act & Assert
        mockMvc.perform(delete("/api/datasource/configs/1/user/100"))
                .andExpect(status().isNoContent());

        verify(databaseConfigService).deleteConfig(1L, 100L);
    }

    @Test
    void testConnection_WhenSucceeds_ShouldReturnSuccess() throws Exception {
        // Arrange
        DatabaseConnectionTestService.ConnectionTestResult result =
                DatabaseConnectionTestService.ConnectionTestResult.success("Connection successful");
        when(databaseConfigService.testExistingConnection(1L, 100L)).thenReturn(result);

        // Act & Assert
        mockMvc.perform(post("/api/datasource/configs/1/user/100/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Connection successful"));

        verify(databaseConfigService).testExistingConnection(1L, 100L);
    }

    @Test
    void testConnection_WhenFails_ShouldReturnFailureWithDetails() throws Exception {
        // Arrange
        DatabaseConnectionTestService.ConnectionTestResult result =
                DatabaseConnectionTestService.ConnectionTestResult.failure(
                        "Connection failed", "SQLException", "08001", 1045);
        when(databaseConfigService.testExistingConnection(1L, 100L)).thenReturn(result);

        // Act & Assert
        mockMvc.perform(post("/api/datasource/configs/1/user/100/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Connection failed"))
                .andExpect(jsonPath("$.exceptionType").value("SQLException"))
                .andExpect(jsonPath("$.sqlState").value("08001"))
                .andExpect(jsonPath("$.errorCode").value(1045));

        verify(databaseConfigService).testExistingConnection(1L, 100L);
    }
}
