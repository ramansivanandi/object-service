package com.objectservice.service;

import com.objectservice.model.DbQueryResult;
import com.objectservice.model.DbServiceEntity;
import com.objectservice.model.DbServiceRequest;
import com.objectservice.repository.DbServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbQueryExecutorService {

    private final DbServiceRepository dbServiceRepository;
    private final DatabaseClient databaseClient;

    // -------------------------------------------------------------------------
    // CRUD for DBService query definitions
    // -------------------------------------------------------------------------

    public Flux<DbServiceEntity> findAll() {
        return dbServiceRepository.findByStatus("ACTIVE");
    }

    public Mono<DbServiceEntity> findById(Long id) {
        return dbServiceRepository.findByIdAndStatus(id, "ACTIVE")
                .switchIfEmpty(Mono.error(new RuntimeException("DBService query not found with id: " + id)));
    }

    @Transactional
    public Mono<DbServiceEntity> create(DbServiceRequest request) {
        DbServiceEntity entity = DbServiceEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .query(request.getQuery())
                .build();
        return dbServiceRepository.save(entity)
                .doOnSuccess(saved -> log.debug("Created DBService query id={}", saved.getId()));
    }

    @Transactional
    public Mono<DbServiceEntity> update(Long id, DbServiceRequest request) {
        return findById(id)
                .flatMap(existing -> {
                    existing.setName(request.getName());
                    existing.setDescription(request.getDescription());
                    existing.setQuery(request.getQuery());
                    return dbServiceRepository.save(existing);
                });
    }

    @Transactional
    public Mono<Void> delete(Long id) {
        return findById(id)
                .flatMap(existing -> {
                    existing.setStatus("DELETED");
                    return dbServiceRepository.save(existing);
                })
                .then();
    }

    // -------------------------------------------------------------------------
    // Dynamic query execution
    // -------------------------------------------------------------------------

    /**
     * Fetches the stored base query by ID, appends the supplied WHERE conditions,
     * executes it reactively and returns a structured result with dynamic rows.
     *
     * Column names in {@code conditions} are validated against [a-zA-Z0-9_.] to
     * prevent SQL injection. Values are always bound as parameters.
     *
     * @param queryId    ID of the DBService record
     * @param conditions map of column -> value to use as WHERE filters
     */
    public Mono<DbQueryResult> executeQuery(Long queryId, Map<String, String> conditions) {
        return findById(queryId)
                .flatMap(dbService -> {
                    String baseQuery = dbService.getQuery().stripLeading();
                    boolean isDml = baseQuery.toUpperCase().matches("^(INSERT|UPDATE|DELETE)\\b.*");

                    if (isDml) {
                        return executeDml(dbService, conditions);
                    } else {
                        return executeSelect(dbService, conditions);
                    }
                });
    }

    private Mono<DbQueryResult> executeSelect(DbServiceEntity dbService, Map<String, String> conditions) {
        String builtSql = buildSql(dbService.getQuery(), conditions);
        log.debug("Executing SELECT id={} sql=[{}] conditions={}", dbService.getId(), builtSql, conditions);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(builtSql);

        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            String paramName = "p_" + sanitize(entry.getKey());
            spec = spec.bind(paramName, coerce(entry.getValue()));
        }

        return spec.fetch().all()
                .map(row -> (Map<String, Object>) new LinkedHashMap<>(row))
                .collectList()
                .map(rows -> new DbQueryResult(
                        dbService.getId(),
                        dbService.getName(),
                        builtSql,
                        rows.size(),
                        rows
                ));
    }

    private Mono<DbQueryResult> executeDml(DbServiceEntity dbService, Map<String, String> params) {
        String sql = dbService.getQuery();
        log.debug("Executing DML id={} sql=[{}] params={}", dbService.getId(), sql, params);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramName = sanitize(entry.getKey());
            spec = spec.bind(paramName, coerce(entry.getValue()));
        }

        return spec.fetch().rowsUpdated()
                .map(rowsAffected -> new DbQueryResult(
                        dbService.getId(),
                        dbService.getName(),
                        sql,
                        rowsAffected.intValue(),
                        Collections.emptyList()
                ));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String buildSql(String baseQuery, Map<String, String> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return baseQuery;
        }

        StringBuilder sql = new StringBuilder(baseQuery.stripTrailing());
        String upper = baseQuery.toUpperCase();
        sql.append(upper.contains("WHERE") ? " AND " : " WHERE ");

        List<String> clauses = new ArrayList<>();
        for (String col : conditions.keySet()) {
            String safe = sanitize(col);
            clauses.add(safe + " = :p_" + safe);
        }
        sql.append(String.join(" AND ", clauses));
        return sql.toString();
    }

    /**
     * Only allows alphanumeric, underscore, and dot (for schema.table.column).
     * Throws if the name contains anything else.
     */
    private String sanitize(String columnName) {
        if (!columnName.matches("[a-zA-Z0-9_.]+")) {
            throw new IllegalArgumentException("Invalid column name: " + columnName);
        }
        return columnName;
    }

    /**
     * Coerces a string value to the most appropriate Java type so R2DBC binds
     * the correct SQL type. Tries Long, then Double, falls back to String.
     */
    private Object coerce(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e2) {
                try {
                    return LocalDate.parse(value);
                } catch (DateTimeParseException e3) {
                    return value;
                }
            }
        }
    }
}
