package com.example.springboot.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Custom query method to find an employee by name
    Optional<Employee> findByName(String name);
}
