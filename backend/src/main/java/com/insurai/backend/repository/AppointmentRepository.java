package com.insurai.backend.repository;

import com.insurai.backend.entity.Employee;
import com.insurai.backend.entity.Appointment;
import com.insurai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime; 
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAgent(User agent);
    List<Appointment> findByEmployee(Employee employee);
    boolean existsByEmployeeAndAppointmentTime(Employee employee, LocalDateTime appointmentTime);
}

//public interface EmployeeRepository extends JpaRepository<Employee, Long> {}
