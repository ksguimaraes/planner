package com.rocketseat.planner.trip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rocketseat.planner.participant.Participant;
import com.rocketseat.planner.participant.dto.ParticipantDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rocketseat.planner.participant.ParticipantService;
import com.rocketseat.planner.participant.dto.ParticipantRequestDTO;
import com.rocketseat.planner.participant.dto.ParticipantResponseDTO;
import com.rocketseat.planner.trip.dto.TripCreateResponseDTO;
import com.rocketseat.planner.trip.dto.TripRequestDTO;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TripRepository tripRepository;
    
    @RequestMapping
    public ResponseEntity<TripCreateResponseDTO> createTrip(@RequestBody TripRequestDTO payload) {
        Trip newTrip = new Trip(payload);

        this.tripRepository.save(newTrip);

        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponseDTO(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestDTO payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if(trip.isPresent() && !trip.get().getIsConfirmed()) {
            Trip rawTrip = trip.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());
            this.tripRepository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
            
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity confirmTrip(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if(trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);
            this.tripRepository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);
            
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantResponseDTO> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestDTO payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if(trip.isPresent()) {
            ParticipantResponseDTO participant = this.participantService.registerParticipantsToEvent(payload.email(), trip.get());
            return ResponseEntity.ok(participant);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantDetailsDTO>> getParticipants(@PathVariable UUID id) {
        List<ParticipantDetailsDTO> participants = this.participantService.getAllParticipantsFromEvent(id);

        return  ResponseEntity.ok(participants);
    }
}
