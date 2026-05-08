package com.objectservice.controller;

import com.objectservice.model.DbQueryResult;
import com.objectservice.model.DbServiceEntity;
import com.objectservice.model.DbServiceRequest;
import com.objectservice.service.DbQueryExecutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dbservice")
@RequiredArgsConstructor
public class DbServiceController {

    private final DbQueryExecutorService dbQueryExecutorService;

    // -------------------------------------------------------------------------
    // CRUD — manage stored query definitions
    // -------------------------------------------------------------------------

    @GetMapping
    public Flux<DbServiceEntity> findAll() {
        return dbQueryExecutorService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<DbServiceEntity> findById(@PathVariable Long id) {
        return dbQueryExecutorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DbServiceEntity> create(@Valid @RequestBody DbServiceRequest request) {
        return dbQueryExecutorService.create(request);
    }

    @PutMapping("/{id}")
    public Mono<DbServiceEntity> update(@PathVariable Long id,
                                        @Valid @RequestBody DbServiceRequest request) {
        return dbQueryExecutorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return dbQueryExecutorService.delete(id);
    }

    // -------------------------------------------------------------------------
    // Execute — run stored query with dynamic WHERE conditions
    //
    // Usage:
    //   GET  /api/v1/dbservice/{id}/execute?status=ACTIVE&name=foo
    //   POST /api/v1/dbservice/{id}/execute  body: {"status":"ACTIVE","name":"foo"}
    //
    // Query params / body keys become WHERE column names.
    // -------------------------------------------------------------------------

    /**
     * Execute via GET — pass WHERE conditions as query parameters.
     * Example: GET /api/v1/dbservice/1/execute?department=HR&status=ACTIVE
     */
    @GetMapping("/{id}/execute")
    public Mono<DbQueryResult> executeGet(
            @PathVariable Long id,
            @RequestParam MultiValueMap<String, String> params) {

        Map<String, String> conditions = new HashMap<>();
        params.forEach((key, values) -> {
            if (!values.isEmpty()) conditions.put(key, values.get(0));
        });
        return dbQueryExecutorService.executeQuery(id, conditions);
    }

    /**
     * Execute via POST — pass WHERE conditions as a JSON body map.
     * Example: POST /api/v1/dbservice/1/execute
     *          body: { "department": "HR", "status": "ACTIVE" }
     */
    @PostMapping("/{id}/execute")
    public Mono<DbQueryResult> executePost(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> conditions) {

        return dbQueryExecutorService.executeQuery(
                id,
                conditions != null ? conditions : new HashMap<>()
        );
    }
}
