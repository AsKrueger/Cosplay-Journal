package com.cosplayjournal.infrastructure.adapter.in.rest.mapper;

import com.cosplayjournal.application.port.in.CreateParticipationCommand;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.participation.Participant;
import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateParticipationRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.ParticipantResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.ParticipationResponse;

import java.util.List;

public class ParticipationRestMapper {

    public static CreateParticipationCommand toCommand(CreateParticipationRequest request) {
        return new CreateParticipationCommand(
                EventId.of(request.eventId()),
                request.cosplayId(),
                request.type(),
                request.leaderUserId(),
                request.leaderName(),
                request.groupName()
        );
    }

    public static ParticipationResponse toResponse(Participation participation) {
        if (participation == null) return null;

        List<ParticipantResponse> participants = participation.getParticipants().stream()
                .map(ParticipationRestMapper::toParticipantResponse)
                .toList();

        return new ParticipationResponse(
                participation.getId().value(),
                participation.getEventId().value(),
                participation.getCosplayId(),
                participation.getType().name(),
                participation.getStatus().name(),
                participation.getGroupName(),
                participants,
                participation.getCreatedAt(),
                participation.getUpdatedAt()
        );
    }

    public static ParticipantResponse toParticipantResponse(Participant participant) {
        if (participant == null) return null;
        return new ParticipantResponse(
                participant.getUserId(),
                participant.getName(),
                participant.getRole().name(),
                participant.getAssignedCharacter(),
                participant.getJoinedAt()
        );
    }
}
