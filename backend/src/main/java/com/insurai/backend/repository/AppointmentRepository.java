package com.insurai.backend.repository;

import com.insurai.backend.entity.Appointment;
import com.insurai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.insurai.backend.entity.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAgentAndStartTimeBetween(User agent, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByAgent(User agent);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByEmployeeId(Long employeeId);
    List<Appointment> findByAgentAndStartTimeLessThanAndEndTimeGreaterThan(User agent, LocalDateTime end, LocalDateTime start);
}
