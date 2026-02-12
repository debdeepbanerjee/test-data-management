package com.example.tdm.controller;

import com.example.tdm.model.dto.DataGenerationRequest;
import com.example.tdm.model.dto.DataGenerationResponse;
import com.example.tdm.service.TestDataManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for test data generation operations
 */
@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@Slf4j
public class DataGenerationController {

    private final TestDataManagementService tdmService;

    /**
     * Generate test data
     * POST /api/v1/data/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<DataGenerationResponse> generateData(
            @Valid @RequestBody DataGenerationRequest request) {
        
        log.info("Received data generation request for schema: {}", request.getSchemaName());
        
        DataGenerationResponse response = tdmService.generateData(request);
        
        HttpStatus status = response.getStatus() == com.example.tdm.model.entity.GeneratedData.GenerationStatus.COMPLETED
                ? HttpStatus.OK
                : HttpStatus.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Get generated data by batch ID
     * GET /api/v1/data/batch/{batchId}
     */
    @GetMapping("/batch/{batchId}")
    public ResponseEntity<DataGenerationResponse> getGeneratedData(
            @PathVariable String batchId) {
        
        log.info("Retrieving generated data for batch: {}", batchId);
        
        DataGenerationResponse response = tdmService.getGeneratedData(batchId);
        return ResponseEntity.ok(response);
    }

    /**
     * Quick generate - simplified endpoint for common scenarios
     * POST /api/v1/data/quick-generate/{schemaName}/{count}
     */
    @PostMapping("/quick-generate/{schemaName}/{count}")
    public ResponseEntity<DataGenerationResponse> quickGenerate(
            @PathVariable String schemaName,
            @PathVariable Integer count) {
        
        log.info("Quick generate request: schema={}, count={}", schemaName, count);
        
        DataGenerationRequest request = DataGenerationRequest.builder()
                .schemaName(schemaName)
                .recordCount(count)
                .persistData(false) // Don't persist for quick generation
                .build();
        
        DataGenerationResponse response = tdmService.generateData(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cleanup expired data
     * DELETE /api/v1/data/cleanup
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredData() {
        log.info("Starting cleanup of expired data");
        
        int cleaned = tdmService.cleanupExpiredData();
        
        return ResponseEntity.ok(
                String.format("Cleaned up %d expired data records", cleaned));
    }
}
