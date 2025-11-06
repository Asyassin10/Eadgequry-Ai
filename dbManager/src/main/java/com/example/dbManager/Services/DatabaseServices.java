package com.example.dbManager.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.dbManager.Dto.Request.DatabaseRequest;
import com.example.dbManager.Dto.Response.DatabaseResponse;
import com.example.dbManager.Models.Database;
import com.example.dbManager.Repositories.DatabaseRepository;

@Service
public class DatabaseServices {
    private final DatabaseRepository repository;

    public DatabaseServices(DatabaseRepository repository) {
        this.repository = repository;
    }

    private DatabaseResponse convert(Database db) {
        return DatabaseResponse.builder()
                .id(db.getId())
                .name(db.getName())
                .host(db.getHost())
                .port(db.getPort())
                .username(db.getUsername())
                .sqlProvider(db.getSqlProvider())
                .build();
    }

    public DatabaseResponse create(DatabaseRequest req) {
        Database db = Database.builder()
                .name(req.getName())
                .host(req.getHost())
                .port(req.getPort())
                .username(req.getUsername())
                .password(req.getPassword())
                .SqlProvider(req.getSqlProvider())
                .build();

        return convert(repository.save(db));

    }

    // ✅ Read all
    public List<DatabaseResponse> getAll() {
        return repository.findAll().stream().map(this::convert).toList();
    }

    // ✅ Read one
    public DatabaseResponse getById(Long id) {
        return convert(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Database not found")));
    }

    // ✅ Update
    public DatabaseResponse update(Long id, DatabaseRequest req) {
        Database db = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Database not found"));

        db.setName(req.getName());
        db.setHost(req.getHost());
        db.setPort(req.getPort());
        db.setUsername(req.getUsername());
        db.setPassword(req.getPassword());
        db.setSqlProvider(req.getSqlProvider());

        return convert(repository.save(db));
    }

    // ✅ Delete
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
