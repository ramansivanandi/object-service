package com.objectservice.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ObjectRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
