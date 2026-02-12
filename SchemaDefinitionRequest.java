package com.example.tdm.model.dto;

import com.example.tdm.model.entity.DataSchema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemaDefinitionRequest {

    @NotBlank(message = "Schema name is required")
    private String schemaName;

    private String description;

    @NotBlank(message = "Schema definition is required")
    private String schemaDefinition; // JSON schema, Java class, or CSV headers

    @NotNull(message = "Schema type is required")
    private DataSchema.SchemaType schemaType;

    private String businessRules; // Natural language constraints

    private String samplePrompt; // Template for AI generation

    @Builder.Default
    private Boolean active = true;

    private String createdBy;
}
