package com.example.tdm.repository;

import com.example.tdm.model.entity.GeneratedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedDataRepository extends JpaRepository<GeneratedData, Long> {

    Optional<GeneratedData> findByBatchId(String batchId);

    List<GeneratedData> findByDataSchemaId(Long schemaId);

    List<GeneratedData> findByStatus(GeneratedData.GenerationStatus status);

    @Query("SELECT g FROM GeneratedData g WHERE g.expiresAt < :now AND g.status = 'COMPLETED'")
    List<GeneratedData> findExpiredData(@Param("now") LocalDateTime now);

    @Query("SELECT g FROM GeneratedData g WHERE g.dataSchema.schemaName = :schemaName ORDER BY g.generatedAt DESC")
    List<GeneratedData> findBySchemaNameOrderByGeneratedAtDesc(@Param("schemaName") String schemaName);

    void deleteByBatchId(String batchId);
}
