package com.objectservice.repository;

import com.objectservice.model.ObjectEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ObjectRepository extends R2dbcRepository<ObjectEntity, Long> {

    Flux<ObjectEntity> findByStatus(String status);

    Mono<ObjectEntity> findByIdAndStatus(Long id, String status);
}
