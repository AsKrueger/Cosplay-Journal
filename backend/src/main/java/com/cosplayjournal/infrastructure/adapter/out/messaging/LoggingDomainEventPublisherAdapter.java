package com.cosplayjournal.infrastructure.adapter.out.messaging;

import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisherAdapter.class);

    @Override
    public void publish(DomainEvent event) {
        if (event != null) {
            log.info("Domain Event Published -> EventId: {}, Type: {}, OccurredOn: {}",
                    event.eventId(), event.getClass().getSimpleName(), event.occurredOn());
        }
    }
}
