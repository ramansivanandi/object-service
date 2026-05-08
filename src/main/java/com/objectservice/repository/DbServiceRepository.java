package com.objectservice.repository;

import com.objectservice.model.DbServiceEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DbServiceRepository extends R2dbcRepository<DbServiceEntity, Long> {

    Flux<DbServiceEntity> findByStatus(String status);

    Mono<DbServiceEntity> findByIdAndStatus(Long id, String status);

    Mono<DbServiceEntity> findByNameAndStatus(String name, String status);
}
