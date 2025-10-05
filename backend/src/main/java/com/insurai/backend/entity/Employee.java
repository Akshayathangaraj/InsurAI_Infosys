package com.insurai.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Primary key referenced by Claim

    private String fullName;
    private String department;
    private String designation;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Employee ↔ Policy
    @ManyToMany
    @JoinTable(
            name = "employee_policies",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "policy_id")
    )
    @JsonIgnoreProperties("assignedEmployee")
    private List<Policy> policies;

    // Employee ↔ Claim
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Claim> claims;

    // Employee ↔ Appointment
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Appointment> appointments;
}
