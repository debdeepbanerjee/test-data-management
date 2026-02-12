package com.example.tdm.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataGenerationRequest {

    @NotBlank(message = "Schema name is required")
    private String schemaName;

    @NotNull(message = "Record count is required")
    @Min(value = 1, message = "Must generate at least 1 record")
    @Max(value = 1000, message = "Cannot generate more than 1000 records at once")
    private Integer recordCount;

    private String customPrompt; // Override default prompt

    private Map<String, Object> additionalConstraints; // Extra rules for this generation

    @Builder.Default
    private Boolean persistData = true; // Whether to save in database

    @Builder.Default
    private String format = "JSON"; // JSON, CSV, SQL

    private Double temperature; // Override default AI temperature

    private String aiModel; // Override default model
}
