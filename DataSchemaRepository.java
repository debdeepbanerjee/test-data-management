package com.example.tdm.repository;

import com.example.tdm.model.entity.DataSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataSchemaRepository extends JpaRepository<DataSchema, Long> {

    Optional<DataSchema> findBySchemaName(String schemaName);

    List<DataSchema> findByActive(Boolean active);

    List<DataSchema> findBySchemaType(DataSchema.SchemaType schemaType);

    boolean existsBySchemaName(String schemaName);
}
