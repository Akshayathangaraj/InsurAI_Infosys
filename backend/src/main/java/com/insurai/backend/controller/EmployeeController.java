package com.insurai.backend.controller;

import com.insurai.backend.dto.EmployeeRequest;
import com.insurai.backend.entity.Employee;
import com.insurai.backend.entity.User;
import com.insurai.backend.repository.EmployeeRepository;
import com.insurai.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    // Create new employee profile
    @PostMapping("/create")
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = new Employee();
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setUser(user);

        return ResponseEntity.ok(employeeRepository.save(employee));
    }

    // Get all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }

    // Get employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update employee
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody Employee updatedEmployee) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setDepartment(updatedEmployee.getDepartment());
                    employee.setDesignation(updatedEmployee.getDesignation());
                    employee.setUser(updatedEmployee.getUser()); // optional
                    return ResponseEntity.ok(employeeRepository.save(employee));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete employee
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employeeRepository.delete(employee);
                    return ResponseEntity.ok("Employee deleted successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Get employee by username for frontend
    @GetMapping("/by-username/{username}")
    public ResponseEntity<Employee> getEmployeeByUsername(@PathVariable String username) {
        return employeeRepository.findByUser_Username(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
