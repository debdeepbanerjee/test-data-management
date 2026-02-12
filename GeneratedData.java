package com.example.tdm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "generated_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GeneratedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schema_id", nullable = false)
    private DataSchema dataSchema;

    @Column(nullable = false)
    private String batchId; // UUID to group related records

    @Column(nullable = false, columnDefinition = "TEXT")
    private String jsonData; // The actual generated data in JSON format

    @Column(nullable = false)
    private Integer recordCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationStatus status;

    @Column(columnDefinition = "TEXT")
    private String promptUsed;

    @Column
    private String aiModel; // Which model generated this

    @Column
    private Double aiTemperature;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private String requestedBy;

    public enum GenerationStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        EXPIRED
    }
}
