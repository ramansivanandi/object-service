package com.objectservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bo_t_dbservice")
public class DbServiceEntity {

    @Id
    private Long id;

    private String name;

    private String description;

    /**
     * Base SQL query stored in the table.
     * Example: SELECT * FROM employees
     * WHERE conditions are appended dynamically at runtime.
     */
    private String query;

    @Builder.Default
    private String status = "ACTIVE";

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
