package com.example.tdm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_schemas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DataSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String schemaName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String schemaDefinition; // JSON schema or Java class definition

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchemaType schemaType; // JAVA_CLASS, JSON_SCHEMA, CSV_TEMPLATE

    @Column(columnDefinition = "TEXT")
    private String businessRules; // Additional constraints for AI

    @Column(columnDefinition = "TEXT")
    private String samplePrompt; // Stored prompt template

    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column
    private String createdBy;

    public enum SchemaType {
        JAVA_CLASS,
        JSON_SCHEMA,
        CSV_TEMPLATE,
        DATABASE_TABLE
    }
}
