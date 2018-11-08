package com.example.demo.controller;
import static com.example.demo.bean.JPADynamicQueryBuilder.*;

import com.example.demo.bean.JPADynamicQueryBuilder;
import com.example.demo.model.TestTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.List;

@RestController
public class TestController {
    @Autowired
    private EntityManager entityManager;

    @GetMapping("/test")
    public ResponseEntity test() {
        Condition condition1 = condition("tb.id", ">=", 10).and().condition("tb.id", "<=", 20);
        Condition condition2 = condition("tb.id", ">", 2).or().condition("tb.id", "=", 17);

        String query = new JPADynamicQueryBuilder()
                .select()
                .from(TestTable.class, "tb")
                .where(
                        condition1.or().condition(condition2)
                ).build();
        List<TestTable> result = entityManager.createQuery(query, TestTable.class).getResultList();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
