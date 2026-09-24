package com.cosplayjournal.domain.model.event;

import com.cosplayjournal.domain.exception.InvalidEventDataException;

import java.time.LocalDate;
import java.util.Objects;

public class Event {

    private final EventId id;
    private String name;
    private String description;
    private EventDateRange dateRange;
    private EventLocation location;
    private String website;
    private final EventSource source;
    private EventStatus status;

    public Event(
            EventId id,
            String name,
            String description,
            EventDateRange dateRange,
            EventLocation location,
            String website,
            EventSource source,
            EventStatus status
    ) {
        validateName(name);
        if (id == null) {
            throw new InvalidEventDataException("El ID del evento no puede ser nulo");
        }
        if (dateRange == null) {
            throw new InvalidEventDataException("El rango de fechas del evento no puede ser nulo");
        }
        if (location == null) {
            throw new InvalidEventDataException("La ubicación del evento no puede ser nula");
        }

        this.id = id;
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.dateRange = dateRange;
        this.location = location;
        this.website = website != null ? website.trim() : "";
        this.source = source != null ? source : EventSource.MANUAL_ADMIN;
        this.status = status != null ? status : EventStatus.SCHEDULED;
    }

    public static Event create(
            EventId id,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            EventLocation location,
            String website,
            EventSource source
    ) {
        return new Event(
                id,
                name,
                description,
                EventDateRange.of(startDate, endDate),
                location,
                website,
                source,
                EventStatus.SCHEDULED
        );
    }

    public void cancel() {
        if (this.status == EventStatus.CANCELLED) {
            return;
        }
        this.status = EventStatus.CANCELLED;
    }

    public void complete() {
        if (this.status == EventStatus.CANCELLED) {
            throw new InvalidEventDataException("No se puede completar un evento que ha sido cancelado");
        }
        this.status = EventStatus.COMPLETED;
    }

    public void changeStatus(EventStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidEventDataException("El estado del evento no puede ser nulo");
        }
        this.status = newStatus;
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidEventDataException("El nombre del evento no puede estar vacío");
        }
        if (name.trim().length() < 2 || name.trim().length() > 150) {
            throw new InvalidEventDataException("El nombre del evento debe tener entre 2 y 150 caracteres");
        }
    }

    public EventId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EventDateRange getDateRange() {
        return dateRange;
    }

    public EventLocation getLocation() {
        return location;
    }

    public String getWebsite() {
        return website;
    }

    public EventSource getSource() {
        return source;
    }

    public EventStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dates=" + dateRange +
                ", city=" + location.city() +
                ", status=" + status +
                '}';
    }
}
