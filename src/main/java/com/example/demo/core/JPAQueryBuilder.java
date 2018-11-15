package com.example.demo.core;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JPAQueryBuilder<T> {
    private AtomicInteger indexer = new AtomicInteger();
    private Map<Integer, Object> params = new HashMap<>();
    private String select;
    private Class<T> resultClass;
    private String from = "";
    private String fromName = "";
    private StringBuilder join = new StringBuilder();
    private StringBuilder where = new StringBuilder();
    private StringBuilder groupBy = new StringBuilder();
    private StringBuilder orderBy = new StringBuilder();
    private int pageSize = -1;
    private StringBuilder cacheQuery;

    /**
     * <p>JPA query SELECT ...
     * <p><i>Example:</i>
     * <p><b>.select</b>(Foo.class, "f")
     * <p><i>Query Result:</i>
     * <p><b>SELECT f</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> select(Class<T> resultClass, String name) {
        this.select = name;
        this.resultClass = resultClass;
        return this;
    }

    /**
     * <p>JPA query SELECT new ...
     * <p><i>Example:</i>
     * <p><b>.selectAsObject</b>(Bar.class, "f.id, f.code")
     * <p><i>Query Result:</i>
     * <p><b>SELECT new com.demo.bar.Bar(f.id, f.code)</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> selectAsObject(Class<T> resultClass, String... params) {
        StringBuilder select = new StringBuilder("new ")
                .append(resultClass.getName())
                .append("(");
        if (params.length >= 1) {
            select.append(params[0]);
        }
        for (int i = 1; i < params.length; i++) {
            select.append(", ").append(params[i]);
        }
        select.append(")");
        this.select = select.toString();
        this.resultClass = resultClass;
        return this;
    }

    /**
     * <p>JPA query FROM ...
     * <p><i>Example:</i>
     * <p><b>.from</b>(Foo.class, "f")
     * <p><i>Query Result:</i>
     * <p>... <b>FROM Foo f</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> from(Class<?> entityClass, String name) {
        this.from = entityClass.getSimpleName();
        this.fromName = name;
        return this;
    }

    /**
     * <p>JPA query WHERE ...
     * <p><i>Example:</i>
     * <p><b>.where</b>(new {@link ValueCondition}("f.id", ">", "1")<b>.and().newValueCondition</b>("f.code", "=", "A")
     * <p><i>Query Result:</i>
     * <p>... <b>WHERE f.id > 1 AND f.code = 'A'</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> where(ValueCondition valueCondition) {
        this.where.append(valueCondition.getValue());
        return this;
    }

    /**
     * <p>JPA query ORDER BY ...
     * <p><i>Example:</i>
     * <p><b>.orderBy</b>("f.id",{@link Direction}<b>.ASC</b>)
     * <p><b>.orderBy</b>("f.code",{@link Direction}<b>.DESC</b>)
     * <p><b>.orderBy</b>({@link JPAQueryBuilder}<b>.count</b>("f.total"),{@link Direction}<b>.ASC</b>)
     * <p><i>Query Result:</i>
     * <p>... <b>ORDER BY f.id ASC, f.code DESC, COUNT(f.total) ASC</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> orderBy(String value, Direction direction) {
        if (this.orderBy.length() > 0) {
            this.orderBy.append(", ");
        }
        this.orderBy.append(value).append(" ").append(direction.name());
        return this;
    }

    /**
     * <p>JPA query GROUP BY ...
     * <p><i>Example:</i>
     * <p><b>.groupBy</b>("f.id")
     * <p><b>.groupBy</b>({@link JPAQueryBuilder}<b>.count</b>("f.total"))
     * <p><i>Query Result:</i>
     * <p>... <b>GROUP BY f.id, COUNT(f.total)</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> groupBy(String value) {
        if (this.groupBy.length() > 0) {
            this.groupBy.append(", ");
        }
        this.groupBy.append(value);
        return this;
    }

    /**
     * <p>JPA query JOIN ...
     * <p><i>Example 1:</i>
     * <p><b>.join</b>({@link JoinType}.<b>INNER_JOIN</b>, "f.items", "i")
     * <p><i>Query Result:</i>
     * <p>... <b>INNER JOIN f.items i</b> ...
     * <p>
     * <p><i>Example 2:</i>
     * <p><b>.join</b>({@link JoinType}.<b>LEFT_JOIN</b>, "f.items", "i")
     * <p><i>Query Result:</i>
     * <p>... <b>LEFT JOIN f.items i</b> ...
     * <p>
     * <p><i>Example 3:</i>
     * <p><b>.join</b>({@link JoinType}.<b>RIGHT_JOIN</b>, "f.items", "i")
     * <p><i>Query Result:</i>
     * <p>... <b>RIGHT JOIN f.items i</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> join(JoinType joinType, String value, String name) {
        this.join.append(joinType.query()).append(" ").append(value)
                .append(" ").append(name).append(" ");
        return this;
    }

    /**
     * <p>JPA query JOIN ... ON ...
     * <p><i>Example 1:</i>
     * <p><b>.joinOn</b>({@link JoinType}.<b>INNER_JOIN</b>, Bar.class, "b", new {@link FieldCondition}("b.fooID", "=", "f.id"))
     * <p><i>Query Result:</i>
     * <p>... <b>INNER JOIN Bar b ON b.fooID = f.id</b> ...
     * <p>
     * <p><i>Example 2:</i>
     * <p><b>.joinOn</b>({@link JoinType}.<b>INNER_JOIN</b>, Bar.class, "b", new {@link FieldCondition}("b.fooID", "=", "f.id")<b>.and</b>("f.id", ">", 5))
     * <p><i>Query Result:</i>
     * <p>... <b>INNER JOIN Bar b ON (b.fooID = f.id and f.id > 5)</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> joinOn(JoinType joinType, Class<?> entityClass, String name,
                                     FieldCondition onCondition) {
        join(joinType, entityClass.getSimpleName(), name);
        this.join.append("ON (").append(onCondition.getValue()).append(") ");
        return this;
    }

    /**
     * <p>JPA query JOIN FETCH ...
     * <p><i>Example:</i>
     * <p><b>.joinFetch</b>({@link JoinType}.<b>INNER_JOIN</b>, "f.items", "i")
     * <p><i>Query Result:</i>
     * <p>... <b>INNER JOIN FETCH f.items i</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> joinFetch(JoinType joinType, String value, String name) {
        this.join.append(joinType.query()).append(" FETCH ").append(value)
                .append(" ").append(name).append(" ");
        return this;
    }

    /**
     * <p>JPA query JOIN FETCH ...
     * <p><i>Example:</i>
     * <p><b>.joinFetch</b>({@link JoinType}.<b>INNER_JOIN</b>, "f.items")
     * <p><i>Query Result:</i>
     * <p>... <b>INNER JOIN FETCH f.items</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> joinFetch(JoinType joinType, String value) {
        this.join.append(joinType.query()).append(" FETCH ").append(value)
                .append(" ");
        return this;
    }

    /**
     * <p>JPA query pagination
     * <p><i>Example:</i>
     * <p><b>.withPagination</b>(10)
     * <p><i>Query Result:</i>
     * <p>... <b>LIMIT 10</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> withPagination(int pageSize) {
        return withPagination(pageSize, -1);
    }

    /**
     * <p>JPA query pagination (limit max)
     * <p><i>Example 1:</i>
     * <p><b>.withPagination</b>(10, 20)
     * <p><i>Query Result:</i>
     * <p>... <b>LIMIT 10</b> ...
     *
     * <p><i>Example 2:</i>
     * <p><b>.withPagination</b>(10, 5)
     * <p><i>Query Result:</i>
     * <p>... <b>LIMIT 5</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public JPAQueryBuilder<T> withPagination(int pageSize, int maxPageSize) {
        if (pageSize >= 1) {
            if (maxPageSize > 0) {
                if (pageSize > maxPageSize) {
                    this.pageSize = maxPageSize;
                } else {
                    this.pageSize = pageSize;
                }
            } else {
                this.pageSize = pageSize;
            }
        }
        return this;
    }

    /**
     * <p>JPA query COUNT (...)
     * <p><i>Example:</i>
     * <p><b>.count("f.id")</b>(10)
     * <p><i>Query Result:</i>
     * <p>... <b>COUNT(f.id)</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public static String count(String value) {
        return "COUNT (" + value + ")";
    }

    /**
     * <p>JPA query DISTINCT (...)
     * <p><i>Example:</i>
     * <p><b>.distinct("f.id")</b>(10)
     * <p><i>Query Result:</i>
     * <p>... <b>DISTINCT(f.id)</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public static String distinct(String value) {
        return "DISTINCT (" + value + ")";
    }

    /**
     * <p>JPA query for tuple values (...)
     * <p><i>Example:</i>
     * <p><b>.tuple("f.id", "f.code", "f.total")</b>(10)
     * <p><i>Query Result:</i>
     * <p>... <b>(f.id,f.tuple,f.total)</b> ...
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public static String tuple(Object... values) {
        StringBuilder result = new StringBuilder("(");
        if (values.length >= 1) {
            result.append(values[0]);
            for (int i = 1; i < values.length; i++) {
                if (values[i] instanceof String) {
                    result.append(",'").append(values[i]).append("'");
                } else {
                    result.append(",").append(values[i]);
                }
            }
        }
        return result.append(")").toString();
    }

    public static String tuple(Iterable<?> objects, String[] fields) throws NoSuchFieldException {
        Type type = objects.getClass().getGenericSuperclass();
        for (Object object : objects) {
            Class<?> objectClass = object.getClass();
            for (String field : fields) {
                Field objectField = objectClass.getDeclaredField(field);

            }
        }
        return "";
    }

    /**
     * <p>Create full query string
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public String buildQuery() {
        StringBuilder query = new StringBuilder().append("SELECT ");
        if (select.length() > 0) {
            query.append(select).append(" ");
        } else {
            query.append(fromName).append(" ");
        }
        if (cacheQuery == null) {
            createQueryAndCache();
        }
        query.append(cacheQuery);
        if (groupBy.length() > 0) {
            query.append("GROUP BY ").append(groupBy).append(" ");
        }
        if (orderBy.length() > 0) {
            query.append("ORDER BY ").append(orderBy).append(" ");
        }
        return query.toString();
    }

    /**
     * <p>Create full count query string
     *
     * @return this {@code JPAQueryBuilder<T>}
     */
    public String buildCountQuery(String countValue) {
        if (cacheQuery == null) {
            createQueryAndCache();
        }
        return "SELECT " + count(countValue) + " " + cacheQuery;
    }

    private void createQueryAndCache() {
        cacheQuery = new StringBuilder()
                .append("FROM ").append(from).append(" ").append(fromName).append(" ");
        if (join.length() > 0) {
            cacheQuery.append(join);
        }
        if (where.length() > 0) {
            cacheQuery.append("WHERE ").append(where).append(" ");
        }
    }

    public Map<Integer, Object> getParams() {
        return params;
    }

    public Class<T> getResultClass() {
        return resultClass;
    }

    public int getPageSize() {
        return pageSize;
    }

    public ValueCondition newValueCondition(String field, String operation, Object value) {
        return new ValueCondition(this.indexer, this.params).condition(field, operation, value);
    }

    public FieldCondition newFieldCondition(String field, String operation, Object value) {
        return new FieldCondition().condition(field, operation, value);
    }

    public static class ValueCondition extends FieldCondition {
        AtomicInteger indexer;
        Map<Integer, Object> params;

        private ValueCondition(AtomicInteger indexer, Map<Integer, Object> params) {
            this.indexer = indexer;
            this.params = params;
        }

        public int getBindParamIndex() {
            return indexer.incrementAndGet();
        }

        public String getBindParamValue(int index) {
            return "?" + index;
        }

        public ValueCondition condition(String field, String operation, Object value) {
            int bindParamIndex = getBindParamIndex();
            String bindParamValue = getBindParamValue(bindParamIndex);
            params.put(bindParamIndex, value);
            super.condition(field, operation, bindParamValue);
            return this;
        }

        public ValueCondition condition(ValueCondition valueCondition) {
            super.condition(valueCondition);
            return this;
        }

        @Override
        public ValueCondition and() {
            super.and();
            return this;
        }

        @Override
        public ValueCondition or() {
            super.or();
            return this;
        }
    }

    public static class FieldCondition {
        boolean hasConjunction = false;
        String conjunction;
        StringBuilder condition = new StringBuilder();

        private FieldCondition() {
        }

        public FieldCondition condition(String field, String operation, Object value) {
            appendConjunction();
            this.condition.append(field).append(" ")
                    .append(operation).append(" ")
                    .append(value);
            return this;
        }

        public FieldCondition condition(FieldCondition fieldCondition) {
            if (this.condition.length() == 0) {
                this.conjunction = null;
                this.condition.append(fieldCondition);
            } else {
                if (this.hasConjunction) {
                    wrapByBracket();
                }
                appendConjunction();
                if (fieldCondition.hasConjunction) {
                    fieldCondition.wrapByBracket();
                }
                this.condition.append(fieldCondition.getValue());

            }
            return this;
        }

        public FieldCondition and() {
            if (condition.length() > 0) {
                this.conjunction = " AND ";
            }
            return this;
        }

        public FieldCondition or() {
            if (condition.length() > 0) {
                this.conjunction = " OR ";
            }
            return this;
        }

        private void appendConjunction() {
            if (this.conjunction != null) {
                this.condition.append(this.conjunction);
                this.conjunction = null;
                this.hasConjunction = true;
            }
        }

        private void wrapByBracket() {
            this.condition.insert(0, "(").append(")");
        }

        protected String getValue() {
            return condition.toString();
        }
    }

    public enum Direction {
        ASC, DESC
    }

    public enum JoinType {
        INNER_JOIN("INNER JOIN"), LEFT_JOIN("LEFT JOIN"), RIGHT_JOIN("RIGHT JOIN");
        String query;

        JoinType(String query) {
            this.query = query;
        }

        public String query() {
            return query;
        }
    }
}
