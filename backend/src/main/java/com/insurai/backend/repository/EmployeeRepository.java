package com.insurai.backend.repository;

import com.insurai.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Fetch employee by linked user's username
    Optional<Employee> findByUser_username(String username);
    Optional<Employee> findByUserId(Long userId);
}
