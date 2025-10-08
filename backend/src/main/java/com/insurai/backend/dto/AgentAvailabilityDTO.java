package com.insurai.backend.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class AgentAvailabilityDTO {

    private Long id;
    private Long agentId;
    private String name; // added
    private int dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isBooked;
    private boolean isOff;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }

    public boolean isOff() { return isOff; }
    public void setOff(boolean off) { isOff = off; }

    // New: computed date string for display
    public String getDate() {
        DayOfWeek dow = DayOfWeek.of(this.dayOfWeek); // 1=Monday
        return dow + " from " + startTime + " to " + endTime;
    }
}
