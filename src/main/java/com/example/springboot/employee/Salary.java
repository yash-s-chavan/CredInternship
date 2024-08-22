package com.example.springboot.employee;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "salary")
@Getter
@Setter
public class Salary {

    @Id
    private Long employeeId; // Employee ID as the primary key

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name = "salary_data", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "weekly_salary")
    private List<Double> salaryData; // 52 doubles representing a year's worth of income

    public Salary() {
    }

    public Salary(Long employeeId, List<Double> salaryData) {
        this.employeeId = employeeId;
        this.salaryData = salaryData;
    }

    @Override
    public String toString() {
        return "Salary{" +
                "employeeId=" + employeeId +
                ", salaryData=" + salaryData +
                '}';
    }
}
