package com.example.dbManager.Dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseResponse {
    private Long id;
    private String name;
    private String host;
    private String port;
    private String username;
    private String sqlProvider;
}
