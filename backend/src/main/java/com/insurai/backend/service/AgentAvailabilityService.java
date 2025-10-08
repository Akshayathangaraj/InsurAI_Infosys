package com.insurai.backend.service;

import com.insurai.backend.dto.AgentAvailabilityDTO;
import com.insurai.backend.dto.AppointmentSlotDTO;
import com.insurai.backend.entity.AgentAvailability;
import com.insurai.backend.entity.User;
import com.insurai.backend.repository.AgentAvailabilityRepository;
import com.insurai.backend.repository.AppointmentRepository;
import com.insurai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentAvailabilityService {

    private final AgentAvailabilityRepository availabilityRepo;
    private final UserRepository userRepo;
    private final AppointmentRepository appointmentRepository;

    public AgentAvailabilityService(AgentAvailabilityRepository availabilityRepo,
                                    UserRepository userRepo,
                                    AppointmentRepository appointmentRepository) {
        this.availabilityRepo = availabilityRepo;
        this.userRepo = userRepo;
        this.appointmentRepository = appointmentRepository;
    }

    public AgentAvailabilityDTO saveSlot(AgentAvailabilityDTO dto) {
        User agent = userRepo.findById(dto.getAgentId())
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        if (!dto.isOff() && dto.getStartTime().compareTo(dto.getEndTime()) >= 0)
            throw new RuntimeException("Start time must be before end time");

        if (!dto.isOff()) {
            List<AgentAvailability> overlapping = availabilityRepo
                    .findByAgentAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
                            agent, dto.getDayOfWeek(), dto.getEndTime(), dto.getStartTime()
                    );
            if (!overlapping.isEmpty())
                throw new RuntimeException("Slot overlaps with existing slot");
        }

        AgentAvailability slot = dto.getId() != null ?
                availabilityRepo.findById(dto.getId()).orElse(new AgentAvailability()) :
                new AgentAvailability();

        slot.setAgent(agent);
        slot.setDayOfWeek(dto.getDayOfWeek());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setOff(dto.isOff());
        slot.setBooked(slot.isBooked());

        return mapToDTO(availabilityRepo.save(slot));
    }

    public AgentAvailabilityDTO toggleOff(Long id) {
        AgentAvailability slot = availabilityRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        slot.setOff(!slot.isOff());
        return mapToDTO(availabilityRepo.save(slot));
    }

    public List<AgentAvailabilityDTO> getAgentSlots(Long agentId) {
        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        return availabilityRepo.findByAgent(agent).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteSlot(Long slotId) {
        availabilityRepo.deleteById(slotId);
    }

    private AgentAvailabilityDTO mapToDTO(AgentAvailability slot) {
        AgentAvailabilityDTO dto = new AgentAvailabilityDTO();
        dto.setId(slot.getId());
        dto.setAgentId(slot.getAgent().getId());
        dto.setDayOfWeek(slot.getDayOfWeek());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setBooked(slot.isBooked());
        dto.setOff(slot.isOff());
        return dto;
    }

    public List<AppointmentSlotDTO> getUpcomingSlots(Long agentId, int daysAhead) {
        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        List<AgentAvailability> weekly = availabilityRepo.findByAgent(agent);
        List<AppointmentSlotDTO> result = new ArrayList<>();
        if (weekly == null || weekly.isEmpty()) return result;

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
            int dowValue = date.getDayOfWeek().getValue();

            for (AgentAvailability avail : weekly) {
                if (avail.isOff() || avail.getDayOfWeek() != dowValue) continue;

                LocalDateTime start = LocalDateTime.of(date, avail.getStartTime());
                LocalDateTime end = LocalDateTime.of(date, avail.getEndTime());

                boolean conflict = appointmentRepository
                        .findByAgentAndStartTimeLessThanAndEndTimeGreaterThan(agent, end, start)
                        .stream().findAny().isPresent();

                if (!conflict) {
                    AppointmentSlotDTO slot = new AppointmentSlotDTO();
                    slot.setAvailabilityId(avail.getId());
                    slot.setStartTime(start);
                    slot.setEndTime(end);
                    result.add(slot);
                }
            }
        }

        result.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        return result;
    }

    public List<AppointmentSlotDTO> getUpcomingSlots(Long agentId) {
        return getUpcomingSlots(agentId, 14);
    }

    public List<AgentAvailabilityDTO> getAllAgents() {
        return availabilityRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}
