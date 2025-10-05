package com.insurai.backend.controller;

import com.insurai.backend.entity.User;
import com.insurai.backend.entity.Role;
import com.insurai.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Get all users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        List<User> users = userRepository.findByRole(userRole);
        return ResponseEntity.ok(users);
    }
}
