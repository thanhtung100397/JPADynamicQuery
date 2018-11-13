package com.example.demo.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "child_table")
public class ChildTable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "test_table_id")
//    private TestTable testTable;
    @Column(name = "test_table_id")
    private String testTableID;
    @Column(name = "value")
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

//    public TestTable getTestTable() {
//        return testTable;
//    }
//
//    public void setTestTable(TestTable testTable) {
//        this.testTable = testTable;
//    }

    public String getTestTableID() {
        return testTableID;
    }

    public void setTestTableID(String testTableID) {
        this.testTableID = testTableID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
