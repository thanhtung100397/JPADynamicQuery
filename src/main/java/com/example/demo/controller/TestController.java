package com.example.demo.controller;

import com.example.demo.core.JPAQueryBuilder.*;

import com.example.demo.core.JPAQueryBuilder;
import com.example.demo.core.JPAQueryExecutor;
import com.example.demo.model.ChildTable;
import com.example.demo.model.TestTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class TestController {
    @Autowired
    private JPAQueryExecutor queryExecutor;

    @GetMapping("/test")
    public ResponseEntity test() {
        JPAQueryBuilder<ChildTable> queryBuilder = new JPAQueryBuilder<>();

        ValueCondition valueCondition1 = queryBuilder
                .newValueCondition()
                .condition("tb.age", ">=", 10)
                .and()
                .condition("tb.age", "<=", 30);

        queryBuilder.select(ChildTable.class, "cb")
                .from(ChildTable.class, "cb")
                .joinOn(JoinType.INNER_JOIN, TestTable.class, "tb",
                        queryBuilder.newValueCondition()
                                .condition("cb.testTableID", "=", "tb.id")
                                .and().condition(queryBuilder.newValueCondition().condition("cb.value", "=", "a")
                                .or().condition("cb.value", "=", "b")))
                .where(
                        valueCondition1
                )
                .orderBy("cb.value", Direction.ASC)
                .withPagination(20);
        List<ChildTable> result = queryExecutor
                .executeQuery(queryBuilder)
                .getResultList();

        Set<TestTable> testTableSet = new HashSet<>();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
