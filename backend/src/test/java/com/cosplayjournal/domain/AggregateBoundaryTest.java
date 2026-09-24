package com.cosplayjournal.domain;

import com.cosplayjournal.domain.event.CosplayCreatedEvent;
import com.cosplayjournal.domain.event.ParticipationCreatedEvent;
import com.cosplayjournal.domain.event.PhotoUploadedEvent;
import com.cosplayjournal.domain.exception.InvalidParticipationDataException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.domain.model.participation.*;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;
import com.cosplayjournal.domain.service.ParticipationValidationDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AggregateBoundaryTest {

    private final ParticipationValidationDomainService validationService = new ParticipationValidationDomainService();

    @Test
    @DisplayName("Participation debe referenciar Event por EventId y Cosplay por cosplayId (no instancias completas)")
    void participationShouldReferenceByValueObjectIds() {
        EventId eventId = EventId.of("evt-999");
        Long cosplayId = 42L;

        Participation participation = Participation.create(
                ParticipationId.of("part-100"),
                eventId,
                cosplayId,
                ParticipationType.INDIVIDUAL,
                Participant.createLeader("user-alpha", "Alonso")
        );

        assertEquals("evt-999", participation.getEventId().value());
        assertEquals(42L, participation.getCosplayId());
    }

    @Test
    @DisplayName("Photo es un Aggregate Root independiente que referencia a Participation por ParticipationId")
    void photoShouldBeIndependentAggregateRootReferencingParticipationId() {
        ParticipationId participationId = ParticipationId.of("part-200");

        Photo photo = Photo.create(
                PhotoId.of("photo-1"),
                participationId,
                "s3://cosplay-photos/part-200/img1.jpg",
                "Foto grupal",
                "user-beta"
        );

        assertNotNull(photo.getId());
        assertEquals("part-200", photo.getParticipationId().value());
        assertEquals("s3://cosplay-photos/part-200/img1.jpg", photo.getStorageReference());
    }

    @Test
    @DisplayName("ParticipationValidationDomainService debe impedir participaciones en eventos cancelados")
    void domainServiceShouldPreventParticipationInCancelledEvent() {
        EventId eventId = EventId.of("evt-cancelled");
        Event event = Event.create(
                eventId, "Evento Cancelado", "Desc",
                LocalDate.now(), LocalDate.now().plusDays(1),
                EventLocation.of("Madrid", "IFEMA"), "", EventSource.MANUAL_ADMIN
        );
        event.cancel();

        Participation participation = Participation.create(
                ParticipationId.of("part-300"),
                eventId,
                10L,
                ParticipationType.INDIVIDUAL,
                Participant.createLeader("u1", "Carlos")
        );

        assertThrows(InvalidParticipationDataException.class, () ->
                validationService.validateParticipationForEvent(participation, event)
        );
    }

    @Test
    @DisplayName("ParticipationValidationDomainService debe validar que exista exactamente 1 líder")
    void domainServiceShouldValidateExactlyOneLeader() {
        Participation participation = Participation.create(
                ParticipationId.of("part-400"),
                EventId.of("evt-1"),
                10L,
                ParticipationType.GROUP,
                Participant.create("u1", "Member 1", ParticipantRole.MEMBER)
        );

        // Sin líder
        assertThrows(InvalidParticipationDataException.class, () ->
                validationService.validateHasExactlyOneLeader(participation)
        );

        // Con 1 líder
        participation.addParticipant(Participant.create("u2", "Leader", ParticipantRole.LEADER));
        assertDoesNotThrow(() -> validationService.validateHasExactlyOneLeader(participation));

        // Con 2 líderes -> error
        participation.addParticipant(Participant.create("u3", "Leader 2", ParticipantRole.LEADER));
        assertThrows(InvalidParticipationDataException.class, () ->
                validationService.validateHasExactlyOneLeader(participation)
        );
    }

    @Test
    @DisplayName("Eventos de dominio deben ser inmutables y contener timestamps e identificadores")
    void domainEventsShouldBeImmutable() {
        CosplayCreatedEvent cosplayEvent = new CosplayCreatedEvent(1L, "Spider-Man");
        assertNotNull(cosplayEvent.eventId());
        assertNotNull(cosplayEvent.occurredOn());
        assertEquals(1L, cosplayEvent.cosplayId());

        ParticipationCreatedEvent partEvent = new ParticipationCreatedEvent("part-1", "evt-1", 10L, ParticipationType.DUO);
        assertEquals("part-1", partEvent.participationId());
        assertEquals(ParticipationType.DUO, partEvent.type());

        PhotoUploadedEvent photoEvent = new PhotoUploadedEvent("photo-1", "part-1", "u1", "path/img.png");
        assertEquals("photo-1", photoEvent.photoId());
        assertEquals("path/img.png", photoEvent.storageReference());
    }
}
