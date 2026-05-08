package com.objectservice.controller;

import com.objectservice.model.ObjectEntity;
import com.objectservice.model.ObjectRequest;
import com.objectservice.service.ObjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;

    @GetMapping
    public Flux<ObjectEntity> findAll() {
        return objectService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ObjectEntity> findById(@PathVariable Long id) {
        return objectService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ObjectEntity> create(@Valid @RequestBody ObjectRequest request) {
        return objectService.create(request);
    }

    @PutMapping("/{id}")
    public Mono<ObjectEntity> update(@PathVariable Long id,
                                     @Valid @RequestBody ObjectRequest request) {
        return objectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return objectService.delete(id);
    }
}
