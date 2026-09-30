package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.event.DomainEvent;

public interface DomainEventPublisherPort {
    void publish(DomainEvent event);
}
