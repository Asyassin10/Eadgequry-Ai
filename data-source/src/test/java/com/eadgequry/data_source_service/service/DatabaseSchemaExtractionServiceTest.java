package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.model.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DatabaseSchemaExtractionServiceTest {

    @InjectMocks
    private DatabaseSchemaExtractionService schemaExtractionService;

    private DatabaseConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new DatabaseConfig();
        testConfig.setDatabaseName("test_db");
        testConfig.setUsername("testuser");
        testConfig.setPassword("testpass");
        testConfig.setHost("localhost");
    }

    @Test
    void buildJdbcUrl_MySQLType_ShouldReturnCorrectUrl() throws Exception {
        // Arrange
        testConfig.setType("mysql");
        testConfig.setPort(3306);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:mysql://"));
        assertTrue(url.contains("localhost"));
        assertTrue(url.contains("3306"));
        assertTrue(url.contains("test_db"));
        assertTrue(url.contains("useSSL=false"));
    }

    @Test
    void buildJdbcUrl_MySQLWithNullPort_ShouldUseDefaultPort() throws Exception {
        // Arrange
        testConfig.setType("mysql");
        testConfig.setPort(null);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertTrue(url.contains("3306")); // Default MySQL port
    }

    @Test
    void buildJdbcUrl_PostgreSQLType_ShouldReturnCorrectUrl() throws Exception {
        // Arrange
        testConfig.setType("postgresql");
        testConfig.setPort(5432);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:postgresql://"));
        assertTrue(url.contains("localhost"));
        assertTrue(url.contains("5432"));
        assertTrue(url.contains("test_db"));
    }

    @Test
    void buildJdbcUrl_PostgreSQLWithNullPort_ShouldUseDefaultPort() throws Exception {
        // Arrange
        testConfig.setType("postgresql");
        testConfig.setPort(null);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertTrue(url.contains("5432")); // Default PostgreSQL port
    }

    @Test
    void buildJdbcUrl_SQLServerType_ShouldReturnCorrectUrl() throws Exception {
        // Arrange
        testConfig.setType("sqlserver");
        testConfig.setPort(1433);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:sqlserver://"));
        assertTrue(url.contains("localhost"));
        assertTrue(url.contains("1433"));
        assertTrue(url.contains("databaseName=test_db"));
    }

    @Test
    void buildJdbcUrl_OracleType_ShouldReturnCorrectUrl() throws Exception {
        // Arrange
        testConfig.setType("oracle");
        testConfig.setPort(1521);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:oracle:thin:@"));
        assertTrue(url.contains("localhost"));
        assertTrue(url.contains("1521"));
        assertTrue(url.contains("test_db"));
    }

    @Test
    void buildJdbcUrl_H2Type_ShouldReturnCorrectUrl() throws Exception {
        // Arrange
        testConfig.setType("h2");
        testConfig.setPort(9092);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:h2:tcp://"));
        assertTrue(url.contains("localhost"));
        assertTrue(url.contains("9092"));
        assertTrue(url.contains("test_db"));
    }

    @Test
    void buildJdbcUrl_UnsupportedType_ShouldThrowException() {
        // Arrange
        testConfig.setType("unsupported-db");
        testConfig.setPort(3306);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> invokeBuildJdbcUrl(testConfig));
    }

    @Test
    void getCatalog_MySQLType_ShouldReturnDatabaseName() throws Exception {
        // Arrange
        testConfig.setType("mysql");

        // Act
        String catalog = invokeGetCatalog(testConfig);

        // Assert
        assertEquals("test_db", catalog);
    }

    @Test
    void getCatalog_PostgreSQLType_ShouldReturnNull() throws Exception {
        // Arrange
        testConfig.setType("postgresql");

        // Act
        String catalog = invokeGetCatalog(testConfig);

        // Assert
        assertNull(catalog);
    }

    @Test
    void getSchemaPattern_PostgreSQLType_WithSchemaName_ShouldReturnSchemaName() throws Exception {
        // Arrange
        testConfig.setType("postgresql");
        testConfig.setSchemaName("custom_schema");

        // Act
        String schema = invokeGetSchemaPattern(testConfig);

        // Assert
        assertEquals("custom_schema", schema);
    }

    @Test
    void getSchemaPattern_PostgreSQLType_WithoutSchemaName_ShouldReturnPublic() throws Exception {
        // Arrange
        testConfig.setType("postgresql");
        testConfig.setSchemaName(null);

        // Act
        String schema = invokeGetSchemaPattern(testConfig);

        // Assert
        assertEquals("public", schema);
    }

    @Test
    void getSchemaPattern_MySQLType_ShouldReturnNull() throws Exception {
        // Arrange
        testConfig.setType("mysql");

        // Act
        String schema = invokeGetSchemaPattern(testConfig);

        // Assert
        assertNull(schema);
    }

    @Test
    void getUpdateDeleteRule_Cascade_ShouldReturnCascade() throws Exception {
        // Act
        String rule = invokeGetUpdateDeleteRule(DatabaseMetaData.importedKeyCascade);

        // Assert
        assertEquals("CASCADE", rule);
    }

    @Test
    void getUpdateDeleteRule_SetNull_ShouldReturnSetNull() throws Exception {
        // Act
        String rule = invokeGetUpdateDeleteRule(DatabaseMetaData.importedKeySetNull);

        // Assert
        assertEquals("SET NULL", rule);
    }

    @Test
    void getUpdateDeleteRule_SetDefault_ShouldReturnSetDefault() throws Exception {
        // Act
        String rule = invokeGetUpdateDeleteRule(DatabaseMetaData.importedKeySetDefault);

        // Assert
        assertEquals("SET DEFAULT", rule);
    }

    @Test
    void getUpdateDeleteRule_Restrict_ShouldReturnRestrict() throws Exception {
        // Act
        String rule = invokeGetUpdateDeleteRule(DatabaseMetaData.importedKeyRestrict);

        // Assert
        assertEquals("RESTRICT", rule);
    }

    @Test
    void getUpdateDeleteRule_NoAction_ShouldReturnNoAction() throws Exception {
        // Act
        String rule = invokeGetUpdateDeleteRule(DatabaseMetaData.importedKeyNoAction);

        // Assert
        assertEquals("NO ACTION", rule);
    }

    @Test
    void getUpdateDeleteRule_Unknown_ShouldReturnUnknown() throws Exception {
        // Act
        String rule = invokeGetUpdateDeleteRule((short) 999);

        // Assert
        assertEquals("UNKNOWN", rule);
    }

    @Test
    void extractSchema_WithInvalidConnection_ShouldThrowRuntimeException() {
        // Arrange
        testConfig.setType("mysql");
        testConfig.setHost("invalid-host-for-schema-extraction");
        testConfig.setPort(9999);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                schemaExtractionService.extractSchema(testConfig));
        assertTrue(exception.getMessage().contains("Schema extraction failed"));
    }

    @Test
    void buildJdbcUrl_CaseInsensitiveType_ShouldWork() throws Exception {
        // Arrange
        testConfig.setType("MySQL"); // Mixed case
        testConfig.setPort(3306);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertTrue(url.startsWith("jdbc:mysql://"));
    }

    @Test
    void buildJdbcUrl_UpperCaseType_ShouldWork() throws Exception {
        // Arrange
        testConfig.setType("POSTGRESQL"); // Upper case
        testConfig.setPort(5432);

        // Act
        String url = invokeBuildJdbcUrl(testConfig);

        // Assert
        assertTrue(url.startsWith("jdbc:postgresql://"));
    }

    // Helper methods to invoke private methods using reflection
    private String invokeBuildJdbcUrl(DatabaseConfig config) throws Exception {
        Method method = DatabaseSchemaExtractionService.class.getDeclaredMethod("buildJdbcUrl", DatabaseConfig.class);
        method.setAccessible(true);
        return (String) method.invoke(schemaExtractionService, config);
    }

    private String invokeGetCatalog(DatabaseConfig config) throws Exception {
        Method method = DatabaseSchemaExtractionService.class.getDeclaredMethod("getCatalog", DatabaseConfig.class);
        method.setAccessible(true);
        return (String) method.invoke(schemaExtractionService, config);
    }

    private String invokeGetSchemaPattern(DatabaseConfig config) throws Exception {
        Method method = DatabaseSchemaExtractionService.class.getDeclaredMethod("getSchemaPattern", DatabaseConfig.class);
        method.setAccessible(true);
        return (String) method.invoke(schemaExtractionService, config);
    }

    private String invokeGetUpdateDeleteRule(short rule) throws Exception {
        Method method = DatabaseSchemaExtractionService.class.getDeclaredMethod("getUpdateDeleteRule", short.class);
        method.setAccessible(true);
        return (String) method.invoke(schemaExtractionService, rule);
    }
}
