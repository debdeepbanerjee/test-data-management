package com.example.tdm.service;

import com.example.tdm.model.dto.DataGenerationRequest;
import com.example.tdm.model.dto.DataGenerationResponse;
import com.example.tdm.model.dto.SchemaDefinitionRequest;
import com.example.tdm.model.entity.DataSchema;
import com.example.tdm.model.entity.GeneratedData;
import com.example.tdm.repository.DataSchemaRepository;
import com.example.tdm.repository.GeneratedDataRepository;
import com.example.tdm.service.ai.AIDataGeneratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main service for test data management operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TestDataManagementService {

    private final AIDataGeneratorService aiDataGeneratorService;
    private final DataSchemaRepository schemaRepository;
    private final GeneratedDataRepository generatedDataRepository;
    private final ObjectMapper objectMapper;

    @Value("${tdm.data.retention-days:30}")
    private int retentionDays;

    @Value("${spring.ai.openai.chat.options.model:gpt-4}")
    private String defaultModel;

    /**
     * Register a new data schema
     */
    @Transactional
    public DataSchema registerSchema(SchemaDefinitionRequest request) {
        log.info("Registering new schema: {}", request.getSchemaName());

        if (schemaRepository.existsBySchemaName(request.getSchemaName())) {
            throw new IllegalArgumentException(
                    "Schema with name '" + request.getSchemaName() + "' already exists");
        }

        DataSchema schema = DataSchema.builder()
                .schemaName(request.getSchemaName())
                .description(request.getDescription())
                .schemaDefinition(request.getSchemaDefinition())
                .schemaType(request.getSchemaType())
                .businessRules(request.getBusinessRules())
                .samplePrompt(request.getSamplePrompt())
                .active(request.getActive())
                .createdBy(request.getCreatedBy())
                .build();

        DataSchema saved = schemaRepository.save(schema);
        log.info("Schema registered successfully with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Generate test data based on a registered schema
     */
    @Transactional
    public DataGenerationResponse generateData(DataGenerationRequest request) {
        long startTime = System.currentTimeMillis();
        String batchId = UUID.randomUUID().toString();

        log.info("Starting data generation for schema: {}, count: {}, batchId: {}",
                request.getSchemaName(), request.getRecordCount(), batchId);

        try {
            // Fetch the schema
            DataSchema schema = schemaRepository.findBySchemaName(request.getSchemaName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Schema not found: " + request.getSchemaName()));

            if (!schema.getActive()) {
                throw new IllegalStateException("Schema is not active: " + request.getSchemaName());
            }

            // Create initial record
            GeneratedData generatedData = createGeneratedDataRecord(
                    schema, batchId, request, GeneratedData.GenerationStatus.IN_PROGRESS);

            // Generate data using AI
            List<?> data = performDataGeneration(schema, request);

            // Convert to JSON
            String jsonData = objectMapper.writeValueAsString(data);

            // Update the record
            generatedData.setJsonData(jsonData);
            generatedData.setRecordCount(data.size());
            generatedData.setStatus(GeneratedData.GenerationStatus.COMPLETED);

            if (request.getPersistData()) {
                generatedDataRepository.save(generatedData);
                log.info("Generated data persisted with batch ID: {}", batchId);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            return buildSuccessResponse(
                    generatedData, data, request.getFormat(), executionTime);

        } catch (Exception e) {
            log.error("Error generating data for batch {}: {}", batchId, e.getMessage(), e);
            
            // Save error record if persistence was requested
            if (request.getPersistData()) {
                saveErrorRecord(batchId, request, e.getMessage());
            }

            long executionTime = System.currentTimeMillis() - startTime;
            return buildErrorResponse(batchId, request, e.getMessage(), executionTime);
        }
    }

    /**
     * Perform the actual data generation based on schema type
     */
    private List<?> performDataGeneration(DataSchema schema, DataGenerationRequest request) {
        String businessRules = buildBusinessRules(schema, request);
        Double temperature = request.getTemperature();

        return switch (schema.getSchemaType()) {
            case JAVA_CLASS -> generateFromJavaClass(schema, request, businessRules, temperature);
            case JSON_SCHEMA -> aiDataGeneratorService.generateFromJsonSchema(
                    schema.getSchemaDefinition(),
                    request.getRecordCount(),
                    businessRules,
                    temperature);
            case CSV_TEMPLATE, DATABASE_TABLE -> throw new UnsupportedOperationException(
                    "Schema type not yet implemented: " + schema.getSchemaType());
        };
    }

    /**
     * Generate data from Java class schema
     */
    private List<?> generateFromJavaClass(
            DataSchema schema,
            DataGenerationRequest request,
            String businessRules,
            Double temperature) {

        // Extract class name from schema definition
        String className = extractClassName(schema.getSchemaDefinition());

        // Use specialized methods for known types
        return switch (className) {
            case "User" -> aiDataGeneratorService.generateUsers(
                    request.getRecordCount(), businessRules, temperature);
            case "Order" -> aiDataGeneratorService.generateOrders(
                    request.getRecordCount(), businessRules, temperature);
            default -> throw new IllegalArgumentException(
                    "Unknown or unsupported Java class: " + className);
        };
    }

    /**
     * Build combined business rules
     */
    private String buildBusinessRules(DataSchema schema, DataGenerationRequest request) {
        StringBuilder rules = new StringBuilder();

        if (schema.getBusinessRules() != null && !schema.getBusinessRules().isEmpty()) {
            rules.append(schema.getBusinessRules());
        }

        if (request.getAdditionalConstraints() != null && !request.getAdditionalConstraints().isEmpty()) {
            if (rules.length() > 0) {
                rules.append("\n\n");
            }
            rules.append("Additional constraints:\n");
            request.getAdditionalConstraints().forEach((key, value) ->
                    rules.append("- ").append(key).append(": ").append(value).append("\n"));
        }

        return rules.toString();
    }

    /**
     * Extract class name from Java class definition
     */
    private String extractClassName(String classDefinition) {
        // Simple extraction - in production, use proper parsing
        if (classDefinition.contains("class User") || classDefinition.contains("record User")) {
            return "User";
        } else if (classDefinition.contains("class Order") || classDefinition.contains("record Order")) {
            return "Order";
        }
        throw new IllegalArgumentException("Cannot extract class name from definition");
    }

    /**
     * Create GeneratedData record
     */
    private GeneratedData createGeneratedDataRecord(
            DataSchema schema,
            String batchId,
            DataGenerationRequest request,
            GeneratedData.GenerationStatus status) {

        return GeneratedData.builder()
                .dataSchema(schema)
                .batchId(batchId)
                .status(status)
                .promptUsed(request.getCustomPrompt())
                .aiModel(request.getAiModel() != null ? request.getAiModel() : defaultModel)
                .aiTemperature(request.getTemperature())
                .expiresAt(LocalDateTime.now().plusDays(retentionDays))
                .requestedBy("system") // In production, get from security context
                .build();
    }

    /**
     * Save error record
     */
    private void saveErrorRecord(String batchId, DataGenerationRequest request, String errorMessage) {
        try {
            DataSchema schema = schemaRepository.findBySchemaName(request.getSchemaName())
                    .orElse(null);

            if (schema != null) {
                GeneratedData errorRecord = GeneratedData.builder()
                        .dataSchema(schema)
                        .batchId(batchId)
                        .status(GeneratedData.GenerationStatus.FAILED)
                        .errorMessage(errorMessage)
                        .recordCount(0)
                        .jsonData("{}")
                        .build();

                generatedDataRepository.save(errorRecord);
            }
        } catch (Exception e) {
            log.error("Failed to save error record: {}", e.getMessage());
        }
    }

    /**
     * Build success response
     */
    private DataGenerationResponse buildSuccessResponse(
            GeneratedData generatedData,
            List<?> data,
            String format,
            long executionTime) throws JsonProcessingException {

        return DataGenerationResponse.builder()
                .batchId(generatedData.getBatchId())
                .schemaName(generatedData.getDataSchema().getSchemaName())
                .recordCount(data.size())
                .status(GeneratedData.GenerationStatus.COMPLETED)
                .generatedAt(generatedData.getGeneratedAt())
                .expiresAt(generatedData.getExpiresAt())
                .data(data)
                .format(format != null ? format : "JSON")
                .aiModel(generatedData.getAiModel())
                .temperature(generatedData.getAiTemperature())
                .promptUsed(generatedData.getPromptUsed())
                .executionTimeMs(executionTime)
                .message("Data generated successfully")
                .build();
    }

    /**
     * Build error response
     */
    private DataGenerationResponse buildErrorResponse(
            String batchId,
            DataGenerationRequest request,
            String errorMessage,
            long executionTime) {

        return DataGenerationResponse.builder()
                .batchId(batchId)
                .schemaName(request.getSchemaName())
                .recordCount(0)
                .status(GeneratedData.GenerationStatus.FAILED)
                .generatedAt(LocalDateTime.now())
                .errorMessage(errorMessage)
                .executionTimeMs(executionTime)
                .message("Data generation failed")
                .build();
    }

    /**
     * Retrieve generated data by batch ID
     */
    public DataGenerationResponse getGeneratedData(String batchId) {
        GeneratedData data = generatedDataRepository.findByBatchId(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        try {
            List<Map<String, Object>> parsedData = objectMapper.readValue(
                    data.getJsonData(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});

            return DataGenerationResponse.builder()
                    .batchId(data.getBatchId())
                    .schemaName(data.getDataSchema().getSchemaName())
                    .recordCount(data.getRecordCount())
                    .status(data.getStatus())
                    .generatedAt(data.getGeneratedAt())
                    .expiresAt(data.getExpiresAt())
                    .data((List<Object>) (List<?>) parsedData)
                    .format("JSON")
                    .aiModel(data.getAiModel())
                    .temperature(data.getAiTemperature())
                    .build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing stored data", e);
        }
    }

    /**
     * List all schemas
     */
    public List<DataSchema> listSchemas() {
        return schemaRepository.findAll();
    }

    /**
     * List all active schemas
     */
    public List<DataSchema> listActiveSchemas() {
        return schemaRepository.findByActive(true);
    }

    /**
     * Get schema by name
     */
    public DataSchema getSchema(String schemaName) {
        return schemaRepository.findBySchemaName(schemaName)
                .orElseThrow(() -> new IllegalArgumentException("Schema not found: " + schemaName));
    }

    /**
     * Delete expired data
     */
    @Transactional
    public int cleanupExpiredData() {
        List<GeneratedData> expiredData = generatedDataRepository.findExpiredData(LocalDateTime.now());
        int count = expiredData.size();
        
        if (count > 0) {
            generatedDataRepository.deleteAll(expiredData);
            log.info("Cleaned up {} expired data records", count);
        }
        
        return count;
    }
}
