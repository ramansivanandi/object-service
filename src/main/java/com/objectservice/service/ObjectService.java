package com.objectservice.service;

import com.objectservice.model.ObjectEntity;
import com.objectservice.model.ObjectRequest;
import com.objectservice.repository.ObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectRepository objectRepository;

    public Flux<ObjectEntity> findAll() {
        return objectRepository.findByStatus("ACTIVE")
                .doOnSubscribe(s -> log.debug("Fetching all active objects"));
    }

    public Mono<ObjectEntity> findById(Long id) {
        return objectRepository.findByIdAndStatus(id, "ACTIVE")
                .switchIfEmpty(Mono.error(new RuntimeException("Object not found with id: " + id)));
    }

    @Transactional
    public Mono<ObjectEntity> create(ObjectRequest request) {
        ObjectEntity entity = ObjectEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return objectRepository.save(entity)
                .doOnSuccess(saved -> log.debug("Created object with id: {}", saved.getId()));
    }

    @Transactional
    public Mono<ObjectEntity> update(Long id, ObjectRequest request) {
        return findById(id)
                .flatMap(existing -> {
                    existing.setName(request.getName());
                    existing.setDescription(request.getDescription());
                    return objectRepository.save(existing);
                })
                .doOnSuccess(updated -> log.debug("Updated object with id: {}", updated.getId()));
    }

    @Transactional
    public Mono<Void> delete(Long id) {
        return findById(id)
                .flatMap(existing -> {
                    existing.setStatus("DELETED");
                    return objectRepository.save(existing);
                })
                .doOnSuccess(deleted -> log.debug("Soft-deleted object with id: {}", deleted.getId()))
                .then();
    }
}
