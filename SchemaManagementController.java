package com.example.tdm.controller;

import com.example.tdm.model.dto.SchemaDefinitionRequest;
import com.example.tdm.model.entity.DataSchema;
import com.example.tdm.service.TestDataManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for schema management operations
 */
@RestController
@RequestMapping("/api/v1/schemas")
@RequiredArgsConstructor
@Slf4j
public class SchemaManagementController {

    private final TestDataManagementService tdmService;

    /**
     * Register a new data schema
     * POST /api/v1/schemas
     */
    @PostMapping
    public ResponseEntity<DataSchema> registerSchema(
            @Valid @RequestBody SchemaDefinitionRequest request) {
        
        log.info("Registering new schema: {}", request.getSchemaName());
        
        DataSchema schema = tdmService.registerSchema(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(schema);
    }

    /**
     * Get all schemas
     * GET /api/v1/schemas
     */
    @GetMapping
    public ResponseEntity<List<DataSchema>> listSchemas(
            @RequestParam(required = false) Boolean activeOnly) {
        
        log.info("Listing schemas, activeOnly={}", activeOnly);
        
        List<DataSchema> schemas = activeOnly != null && activeOnly
                ? tdmService.listActiveSchemas()
                : tdmService.listSchemas();
        
        return ResponseEntity.ok(schemas);
    }

    /**
     * Get schema by name
     * GET /api/v1/schemas/{schemaName}
     */
    @GetMapping("/{schemaName}")
    public ResponseEntity<DataSchema> getSchema(@PathVariable String schemaName) {
        log.info("Retrieving schema: {}", schemaName);
        
        DataSchema schema = tdmService.getSchema(schemaName);
        return ResponseEntity.ok(schema);
    }

    /**
     * Health check endpoint
     * GET /api/v1/schemas/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("TDM Service is running");
    }
}
