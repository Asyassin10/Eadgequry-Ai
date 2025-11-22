package com.eadgequry.chat_bot_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SqlValidatorServiceTest {

    @InjectMocks
    private SqlValidatorService sqlValidatorService;

    @Test
    void cleanQuery_ShouldRemoveBackticksAndTrim() {
        // Arrange
        String dirtyQuery = "```sql\nSELECT * FROM users\n```";

        // Act
        String cleaned = sqlValidatorService.cleanQuery(dirtyQuery);

        // Assert
        assertEquals("SELECT * FROM users", cleaned);
    }

    @Test
    void cleanQuery_ShouldHandleSimpleQuery() {
        // Arrange
        String query = "SELECT * FROM users";

        // Act
        String cleaned = sqlValidatorService.cleanQuery(query);

        // Assert
        assertEquals("SELECT * FROM users", cleaned);
    }

    @Test
    void cleanQuery_ShouldRemoveMarkdownCodeBlocks() {
        // Arrange
        String query = "```SELECT * FROM users```";

        // Act
        String cleaned = sqlValidatorService.cleanQuery(query);

        // Assert
        assertEquals("SELECT * FROM users", cleaned);
    }
}
