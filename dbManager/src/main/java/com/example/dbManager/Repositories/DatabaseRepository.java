package com.example.dbManager.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dbManager.Models.Database;

@Repository
public interface DatabaseRepository extends JpaRepository<Database, Long> {

}
