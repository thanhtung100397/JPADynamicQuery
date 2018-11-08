package com.example.demo.repository;

import com.example.demo.model.TestTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestTableRepository extends JpaRepository<TestTable, String> {
}
