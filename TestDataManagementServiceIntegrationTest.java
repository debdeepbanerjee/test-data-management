package com.example.tdm.service;

import com.example.tdm.model.dto.DataGenerationRequest;
import com.example.tdm.model.dto.DataGenerationResponse;
import com.example.tdm.model.dto.SchemaDefinitionRequest;
import com.example.tdm.model.entity.DataSchema;
import com.example.tdm.model.entity.GeneratedData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Test Data Management Service
 */
@SpringBootTest
@ActiveProfiles("test")
class TestDataManagementServiceIntegrationTest {

    @Autowired
    private TestDataManagementService tdmService;

    @Test
    void testRegisterSchema() {
        SchemaDefinitionRequest request = SchemaDefinitionRequest.builder()
                .schemaName("TestUser_" + System.currentTimeMillis())
                .description("Test user schema")
                .schemaDefinition("public record TestUser(String name, String email) {}")
                .schemaType(DataSchema.SchemaType.JAVA_CLASS)
                .businessRules("Email must be valid")
                .active(true)
                .createdBy("test")
                .build();

        DataSchema schema = tdmService.registerSchema(request);

        assertNotNull(schema);
        assertNotNull(schema.getId());
        assertEquals(request.getSchemaName(), schema.getSchemaName());
        assertTrue(schema.getActive());
    }

    @Test
    void testGenerateUserData() {
        DataGenerationRequest request = DataGenerationRequest.builder()
                .schemaName("User")
                .recordCount(5)
                .persistData(false)
                .build();

        DataGenerationResponse response = tdmService.generateData(request);

        assertNotNull(response);
        assertEquals(GeneratedData.GenerationStatus.COMPLETED, response.getStatus());
        assertEquals(5, response.getRecordCount());
        assertNotNull(response.getData());
        assertEquals(5, response.getData().size());
        assertNotNull(response.getBatchId());
    }

    @Test
    void testGenerateOrderData() {
        DataGenerationRequest request = DataGenerationRequest.builder()
                .schemaName("Order")
                .recordCount(3)
                .persistData(false)
                .build();

        DataGenerationResponse response = tdmService.generateData(request);

        assertNotNull(response);
        assertEquals(GeneratedData.GenerationStatus.COMPLETED, response.getStatus());
        assertEquals(3, response.getRecordCount());
        assertNotNull(response.getData());
    }

    @Test
    void testGenerateWithCustomConstraints() {
        DataGenerationRequest request = DataGenerationRequest.builder()
                .schemaName("User")
                .recordCount(2)
                .persistData(false)
                .additionalConstraints(java.util.Map.of(
                        "age_range", "25-35",
                        "location", "California only"
                ))
                .build();

        DataGenerationResponse response = tdmService.generateData(request);

        assertNotNull(response);
        assertEquals(GeneratedData.GenerationStatus.COMPLETED, response.getStatus());
    }

    @Test
    void testListSchemas() {
        var schemas = tdmService.listSchemas();
        assertNotNull(schemas);
        assertTrue(schemas.size() >= 2); // At least User and Order
    }

    @Test
    void testListActiveSchemas() {
        var activeSchemas = tdmService.listActiveSchemas();
        assertNotNull(activeSchemas);
        assertTrue(activeSchemas.stream().allMatch(DataSchema::getActive));
    }

    @Test
    void testInvalidSchemaName() {
        DataGenerationRequest request = DataGenerationRequest.builder()
                .schemaName("NonExistentSchema")
                .recordCount(5)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            tdmService.generateData(request);
        });
    }
}
