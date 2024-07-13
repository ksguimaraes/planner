package com.rocketseat.planner.participant.dto;

import java.util.UUID;

public record ParticipantDetailsDTO(UUID id, String name, String email, Boolean isConfirmed) {
}
