package com.example.springboot.employee;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "employee")
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Simplified ID

    private String name;  // Simplified property names

    private String state;

    @Override
    public String toString() {
        return "Employee{" +
                "ID=" + id +
                ", Name='" + name + '\'' +
                ", State='" + state + '\'' +
                '}';
    }
}
