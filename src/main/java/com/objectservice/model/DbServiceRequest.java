package com.objectservice.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DbServiceRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Query is required")
    private String query;
}
