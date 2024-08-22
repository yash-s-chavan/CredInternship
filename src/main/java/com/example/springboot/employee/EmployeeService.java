package com.example.springboot.employee;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ExecutorService executorService;
    private final SalaryRepository salaryRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, SalaryRepository salaryRepository) {
        this.employeeRepository = employeeRepository;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.salaryRepository = salaryRepository;
    }

    public List<Employee> getEmployees() {
        Callable<List<Employee>> task = () -> employeeRepository.findAll();
        Future<List<Employee>> future = executorService.submit(task);

        try {
            log.info("Fetching all employees");
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to fetch employees");
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }


    public void addNewEmployee(Employee employee) {
        executorService.submit(() -> {
            log.info("Adding new employee with name: {}", employee.getName());
            Optional<Employee> existingEmployee = employeeRepository.findByName(employee.getName());
            if (existingEmployee.isPresent()) {
                log.warn("Employee with name '{}' already exists", employee.getName());
                throw new IllegalStateException("Employee already exists");
            }
            employeeRepository.save(employee);
            log.info("Successfully added new employee");
        });
    }

    public void deleteEmployee(Long employeeId) {
        log.info("Deleting employee with id: {}", employeeId);
        executorService.submit(() -> {
            boolean exists = employeeRepository.existsById(employeeId);
            if (!exists) {
                log.warn("Employee with id {} does not exist", employeeId);
                throw new IllegalStateException("Employee not found");
            }
            employeeRepository.deleteById(employeeId);
            log.info("Successfully deleted employee with id: {}", employeeId);
        });
    }

    @Transactional
    public void updateEmployee(Long employeeId, String name, String state) {
        log.info("Updating employee with id: {}", employeeId);
        executorService.submit(() -> {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalStateException("Employee not found"));

            if (name != null && !name.isBlank() && !Objects.equals(employee.getName(), name)) {
                employee.setName(name);
            }

            if (state != null && !state.isBlank() && !Objects.equals(employee.getState(), state)) {
                employee.setState(state);
            }

            employeeRepository.save(employee);
            log.info("Successfully updated employee with id: {}", employeeId);
        });
    }

    @Transactional
    public void generateSampleEmployees(int numberOfEmployees) {
        Random random = new Random();
        for (int i = 0; i < numberOfEmployees; i++) {
            Employee employee = new Employee();
            employee.setName("Employee " + (i + 1));
            employee.setState("PA");
            employeeRepository.save(employee);

            // Generate weekly income for a year (52 weeks)
            List<Double> weeklyIncome = new ArrayList<>();
            for (int week = 0; week < 52; week++) {
                weeklyIncome.add(500 + (1000 - 500) * random.nextDouble());
            }

            // Save the salary data
            Salary salary = new Salary(employee.getId(), weeklyIncome);
            salaryRepository.save(salary);
        }
    }



    public List<Double> getSalaryDataForEmployee(Long employeeId) {
        return salaryRepository.findByEmployeeId(employeeId).stream()
                .flatMap(salaryData -> salaryData.getSalaryData().stream())
                .collect(Collectors.toList());
    }



    //Logging
    //github



    public Map<Long, Double> calculateAverageSalariesWithPlatformThreads() {
        List<Employee> employees = getEmployees();
        Map<Long, Double> averageSalaries = new HashMap<>();

        // Creating a fixed thread pool using platform threads
        ExecutorService platformExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Callable<Void>> tasks = new ArrayList<>();

        for (Employee employee : employees) {
            tasks.add(() -> {
                List<Double> salaryData = getSalaryDataForEmployee(employee.getId());

                double averageSalary = salaryData.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

                synchronized (averageSalaries) {
                    averageSalaries.put(employee.getId(), averageSalary);
                }
                return null;
            });
        }

        try {
            List<Future<Void>> futures = platformExecutorService.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get(); // Ensuring all tasks are complete
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to calculate average salaries", e);
        } finally {
            platformExecutorService.shutdown();
        }

        return averageSalaries;
    }


    public Map<Long, Double> calculateAverageSalaries() {
        List<Employee> employees = getEmployees();
        Map<Long, Double> averageSalaries = new HashMap<>();

        // Create a new ExecutorService if it is not already created elsewhere
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        List<Callable<Void>> tasks = new ArrayList<>();

        for (Employee employee : employees) {
            tasks.add(() -> {
                List<Double> salaryData = getSalaryDataForEmployee(employee.getId());

                double averageSalary = salaryData.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

                synchronized (averageSalaries) {
                    averageSalaries.put(employee.getId(), averageSalary);
                }
                return null;
            });
        }

        try {
            List<Future<Void>> futures = executorService.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get(); // Ensuring all tasks are complete
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to calculate average salaries", e);
        } finally {
            shutdownExecutor(executorService); // Ensure executor is shut down
        }

        return averageSalaries;
    }

    private void shutdownExecutor(ExecutorService executorService) {
        executorService.shutdown(); // Initiate shutdown
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Force shutdown if not terminated
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }





    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearDatabase() {
        employeeRepository.deleteAll();
        log.info("Cleared all employees from the database");
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}
