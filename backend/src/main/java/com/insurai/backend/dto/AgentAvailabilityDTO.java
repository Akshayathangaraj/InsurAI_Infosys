package com.insurai.backend.dto;

import java.time.LocalTime;

public class AgentAvailabilityDTO {

    private Long id;
    private Long agentId;
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
}
