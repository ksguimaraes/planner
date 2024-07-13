package com.rocketseat.planner.participant;

import java.util.List;
import java.util.UUID;

import com.rocketseat.planner.participant.dto.ParticipantDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rocketseat.planner.participant.dto.ParticipantResponseDTO;
import com.rocketseat.planner.trip.Trip;

@Service
public class ParticipantService {
    @Autowired
    private ParticipantRepository participantRepository;

    public void registerParticipantsToEvent(List<String> participantsToInvite, Trip trip) {
        List<Participant> participants = participantsToInvite.stream().map(email -> new Participant(email, trip)).toList();

        this.participantRepository.saveAll(participants);
    }

    public ParticipantResponseDTO registerParticipantsToEvent(String email, Trip trip) {
        Participant participant = new Participant(email, trip);
        this.participantRepository.save(participant);

        return new ParticipantResponseDTO(participant.getId());
    }

    public void triggerConfirmationEmailToParticipants(UUID tripId) {}

    public List<ParticipantDetailsDTO> getAllParticipantsFromEvent(UUID tripId) {
        return this.participantRepository.findByTripId(tripId).stream().map(participant -> new ParticipantDetailsDTO(participant.getId(), participant.getName(), participant.getEmail(), participant.getIsConfirmed())).toList();
    }
}
