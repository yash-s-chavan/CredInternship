package com.example.springboot.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/v1/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    @GetMapping("generate")
    public void generateEmployees(@RequestParam int count) {
       employeeService.generateSampleEmployees(count);
    }

    @GetMapping("average")
    public Map<Long, Double> averageEmployees(){
        return employeeService.calculateAverageSalaries();
    }
    @GetMapping("average-platform")
    public Map<Long, Double> averagePlatformEmployees(){
        return employeeService.calculateAverageSalariesWithPlatformThreads();
    }

    @GetMapping
    public List<Employee> getEmployees() {
        return employeeService.getEmployees();
    }

    @PostMapping
    public void postEmployee(@RequestBody Employee employee) {
        employeeService.addNewEmployee(employee);
    }

    @DeleteMapping(path = "{employeeId}")
    public void deleteEmployee(@PathVariable("employeeId") Long employeeId) {
        employeeService.deleteEmployee(employeeId);
    }

    @GetMapping("reset")
    public void resetEmployees() {
        employeeService.clearDatabase();
    }

    @PutMapping(path = "{employeeId}")
    public void updateEmployee(@PathVariable("employeeId") Long employeeId,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) String state) {
        employeeService.updateEmployee(employeeId, name, state);
    }
}
