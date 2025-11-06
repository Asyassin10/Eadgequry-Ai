package com.example.dbManager.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DatabaseRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Host is required")
    private String host;

    @NotBlank(message = "Port is required")
    private String port;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "SQL Provider is required")
    private String sqlProvider;
}