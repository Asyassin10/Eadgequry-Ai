package com.example.dbManager.Controllers;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dbManager.Dto.Request.DatabaseRequest;
import com.example.dbManager.Dto.Response.DatabaseResponse;
import com.example.dbManager.Services.DatabaseServices;

@RestController
@RequestMapping("/databases")
public class DatabaseController {
    private final DatabaseServices databaseService;

    public DatabaseController(DatabaseServices databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping
    public DatabaseResponse create(@Validated @RequestBody DatabaseRequest request) {
        return databaseService.create(request);
    }

    @GetMapping
    public List<DatabaseResponse> getAll() {
        return databaseService.getAll();
    }

    @GetMapping("/{id}")
    public DatabaseResponse getById(@PathVariable Long id) {
        return databaseService.getById(id);
    }

    @PutMapping("/{id}")
    public DatabaseResponse update(@PathVariable Long id, @Validated @RequestBody DatabaseRequest request) {
        return databaseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        databaseService.delete(id);
    }
}
