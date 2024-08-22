package com.example.springboot.employee;

import java.util.List;

public class FetchTask implements Runnable {
    private final EmployeeService employeeService;
    public FetchTask(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void run(){
        System.out.println("Fetching employees on thread" + Thread.currentThread().getName());
        List<Employee> employees = employeeService.getEmployees();
    }
}