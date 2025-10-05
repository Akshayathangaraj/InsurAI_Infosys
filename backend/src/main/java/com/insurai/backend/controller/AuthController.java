package com.insurai.backend.controller;

import com.insurai.backend.dto.LoginRequest;
import com.insurai.backend.dto.SignupRequest;
import com.insurai.backend.entity.User;
import com.insurai.backend.repository.UserRepository;
import com.insurai.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.insurai.backend.entity.Role;
import com.insurai.backend.entity.Employee;
import com.insurai.backend.repository.EmployeeRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        return ResponseEntity.badRequest().body("Username is already taken!");
    }
    if (userRepository.existsByEmail(request.getEmail())) {
        return ResponseEntity.badRequest().body("Email is already in use!");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    Role role;
    try {
        role = Role.valueOf(request.getRole().toUpperCase());
        user.setRole(role);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Invalid role provided. Must be EMPLOYEE, ADMIN, or AGENT.");
    }

    userRepository.save(user);

    // ✅ If role is EMPLOYEE, create an Employee entry
    if (role == Role.EMPLOYEE) {
        Employee employee = new Employee();
        employee.setUser(user);
       // employee.setFullName(request.getName() != null ? request.getName() : request.getUsername());
        employeeRepository.save(employee);
    }

    return ResponseEntity.ok("User registered successfully!");
}

  @PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    User user = userRepository.findByUsername(request.getUsername()).orElse(null);

    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

    Map<String, Object> response = new HashMap<>();
    response.put("token", token);
    response.put("username", user.getUsername());
    response.put("role", user.getRole());
    response.put("userId", user.getId()); // <-- ALWAYS set userId for all users!

    // If the role is EMPLOYEE, fetch the employeeId
    if (user.getRole() == Role.EMPLOYEE) {
        Employee employee = employeeRepository.findByUser_username(user.getUsername())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        response.put("employeeId", employee.getId());
    }

    return ResponseEntity.ok(response);
}


}
