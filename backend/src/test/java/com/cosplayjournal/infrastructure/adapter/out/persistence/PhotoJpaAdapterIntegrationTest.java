package com.cosplayjournal.infrastructure.adapter.out.persistence;

import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.domain.model.participation.Participant;
import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.participation.ParticipationType;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaCosplayRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaEventRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaParticipationRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaPhotoRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PhotoJpaAdapterIntegrationTest {

    @Autowired
    private JpaPhotoRepositoryAdapter photoRepositoryAdapter;

    @Autowired
    private JpaParticipationRepositoryAdapter participationRepositoryAdapter;

    @Autowired
    private JpaEventRepositoryAdapter eventRepositoryAdapter;

    @Autowired
    private JpaCosplayRepositoryAdapter cosplayRepositoryAdapter;

    @Test
    @DisplayName("Debe guardar, buscar por ParticipationId y eliminar Photo en la base de datos relacional")
    void shouldSaveAndFindPhotoInDatabase() {
        // Claves foráneas
        Event event = eventRepositoryAdapter.save(Event.create(
                EventId.of("evt-photo-1"), "Photo Fest", "Desc",
                LocalDate.now(), LocalDate.now(), EventLocation.of("Sevilla", "Fibes"), "", EventSource.MANUAL_ADMIN
        ));
        Cosplay cosplay = cosplayRepositoryAdapter.save(Cosplay.createNew("Naruto", "Desc", "Naruto", "Anime"));

        ParticipationId participationId = ParticipationId.of("part-photo-1");
        participationRepositoryAdapter.save(Participation.create(
                participationId, event.getId(), cosplay.getId(), ParticipationType.INDIVIDUAL, Participant.createLeader("u1", "Carlos")
        ));

        PhotoId photoId = PhotoId.of("photo-100");
        Photo photo = Photo.create(
                photoId, participationId, "s3://bucket/part-photo-1/img.png", "Foto en photocall", "u1"
        );

        Photo saved = photoRepositoryAdapter.save(photo);

        assertNotNull(saved);
        assertEquals("photo-100", saved.getId().value());

        Optional<Photo> found = photoRepositoryAdapter.findById(photoId);
        assertTrue(found.isPresent());
        assertEquals("Foto en photocall", found.get().getCaption());

        List<Photo> photosByPart = photoRepositoryAdapter.findByParticipationId(participationId);
        assertEquals(1, photosByPart.size());

        photoRepositoryAdapter.deleteById(photoId);
        assertTrue(photoRepositoryAdapter.findById(photoId).isEmpty());
    }
}
