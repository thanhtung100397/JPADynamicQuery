package com.example.demo.bean;

public class JPADynamicQueryBuilder {
    private StringBuilder select = new StringBuilder();
    private String from = "";
    private String fromName = "";
    private StringBuilder where = new StringBuilder();

    public JPADynamicQueryBuilder select() {
        return this;
    }

    public JPADynamicQueryBuilder from(Class<?> entityClass, String name) {
        this.from = entityClass.getSimpleName();
        this.fromName = name;
        return this;
    }

    public JPADynamicQueryBuilder where(Condition condition) {
        this.where.append(condition.getValue());
        return this;
    }

    public String build() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ").append(select.length() > 0 ? select : fromName).append(" ")
                .append("FROM ").append(from).append(" ").append(fromName).append(" ");
        if (where.length() > 0) {
            query.append("WHERE ").append(where);
        }
        return query.toString();
    }

    public static Condition condition(String field, String operation, String value) {
        return new Condition().condition(field, operation, value);
    }

    public static Condition condition(String field, String operation, int value) {
        return new Condition().condition(field, operation, value);
    }

    public static class Condition {
        String conjunction;
        StringBuilder condition = new StringBuilder();

        public Condition condition(String field, String operation, String value) {
            appendConjunction();
            this.condition.append(field).append(" ")
                    .append(operation)
                    .append(" '").append(value).append("'");
            return this;
        }

        public Condition condition(String field, String operation, int value) {
            appendConjunction();
            this.condition.append(field).append(" ")
                    .append(operation)
                    .append(" ").append(value);
            return this;
        }

        public Condition condition(Condition condition) {
            if (this.condition.length() == 0) {
                this.conjunction = null;
                this.condition.append(condition);
            } else {
                this.condition.insert(0, "(").append(")");
                appendConjunction();
                this.condition.append("(")
                        .append(condition.getValue())
                        .append(")");
            }
            return this;
        }

        public Condition and() {
            if (condition.length() > 0) {
                this.conjunction = " AND ";
            }
            return this;
        }

        public Condition or() {
            if (condition.length() > 0) {
                this.conjunction = " OR ";
            }
            return this;
        }

        private void appendConjunction() {
            if (this.conjunction != null) {
                this.condition.append(this.conjunction);
                this.conjunction = null;
            }
        }

        private String getValue() {
            return condition.toString();
        }
    }
}
