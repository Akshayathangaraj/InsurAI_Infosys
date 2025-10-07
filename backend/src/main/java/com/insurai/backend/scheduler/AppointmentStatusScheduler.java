package com.insurai.backend.scheduler;

import com.insurai.backend.service.AppointmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppointmentStatusScheduler {

    private final AppointmentService appointmentService;

    public AppointmentStatusScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void checkAndMarkMissedAppointments() {
        appointmentService.markMissedAppointments();
    }
}
