package com.example.demo.core;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Map;

@Component
public class JPAQueryExecutor {
    private EntityManager entityManager;

    public JPAQueryExecutor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> TypedQuery<T> executeQuery(JPAQueryBuilder<T> queryBuilder) {
        TypedQuery<T> typedQuery = entityManager
                .createQuery(queryBuilder.buildQuery(), queryBuilder.getResultClass());
        for(Map.Entry<Integer, Object> paramEntry : queryBuilder.getParams().entrySet()) {
            typedQuery.setParameter(paramEntry.getKey(), paramEntry.getValue());
        }
        if (queryBuilder.getPageSize() > 0) {
            typedQuery.setMaxResults(queryBuilder.getPageSize());
        }
        return typedQuery;
    }

    public <T> long executeCountQuery(String countValue, JPAQueryBuilder<T> queryBuilder) {
        TypedQuery<Long> typedQuery = entityManager
                .createQuery(queryBuilder.buildCountQuery(countValue), Long.class);
        for(Map.Entry<Integer, Object> paramEntry : queryBuilder.getParams().entrySet()) {
            typedQuery.setParameter(paramEntry.getKey(), paramEntry.getValue());
        }
        if (queryBuilder.getPageSize() > 0) {
            typedQuery.setMaxResults(queryBuilder.getPageSize());
        }
        Long count = typedQuery.getSingleResult();
        return count == null? 0 : count;
    }
}
