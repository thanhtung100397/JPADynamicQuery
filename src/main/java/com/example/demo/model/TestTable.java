package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "test_table")
public class TestTable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private int age;
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "testTable")
//    @JsonIgnore
//    private List<ChildTable> childTables;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

//    public List<ChildTable> getChildTables() {
//        return childTables;
//    }
//
//    public void setChildTables(List<ChildTable> childTables) {
//        this.childTables = childTables;
//    }
}
