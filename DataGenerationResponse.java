package com.example.tdm.model.dto;

import com.example.tdm.model.entity.GeneratedData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataGenerationResponse {

    private String batchId;
    private String schemaName;
    private Integer recordCount;
    private GeneratedData.GenerationStatus status;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    
    // The actual generated data
    private List<Object> data; // Can be List<Map<String, Object>> or typed objects
    
    private String format; // JSON, CSV, SQL
    private String downloadUrl; // For large datasets
    
    // Metadata
    private String aiModel;
    private Double temperature;
    private String promptUsed;
    private Long executionTimeMs;
    
    private String message;
    private String errorMessage;
}
