package com.cosplayjournal.infrastructure.config;

import com.cosplayjournal.application.port.out.*;
import com.cosplayjournal.application.service.*;
import com.cosplayjournal.domain.service.ParticipationValidationDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public ParticipationValidationDomainService participationValidationDomainService() {
        return new ParticipationValidationDomainService();
    }

    @Bean
    public CosplayApplicationService cosplayApplicationService(
            CosplayRepositoryPort cosplayRepositoryPort,
            DomainEventPublisherPort domainEventPublisherPort
    ) {
        return new CosplayApplicationService(cosplayRepositoryPort, domainEventPublisherPort);
    }

    @Bean
    public EventApplicationService eventApplicationService(
            EventRepositoryPort eventRepositoryPort,
            DomainEventPublisherPort domainEventPublisherPort
    ) {
        return new EventApplicationService(eventRepositoryPort, domainEventPublisherPort);
    }

    @Bean
    public ParticipationApplicationService participationApplicationService(
            ParticipationRepositoryPort participationRepositoryPort,
            EventRepositoryPort eventRepositoryPort,
            CosplayRepositoryPort cosplayRepositoryPort,
            DomainEventPublisherPort domainEventPublisherPort,
            ParticipationValidationDomainService validationDomainService
    ) {
        return new ParticipationApplicationService(
                participationRepositoryPort,
                eventRepositoryPort,
                cosplayRepositoryPort,
                domainEventPublisherPort,
                validationDomainService
        );
    }

    @Bean
    public PhotoApplicationService photoApplicationService(
            PhotoRepositoryPort photoRepositoryPort,
            ParticipationRepositoryPort participationRepositoryPort,
            DomainEventPublisherPort domainEventPublisherPort
    ) {
        return new PhotoApplicationService(photoRepositoryPort, participationRepositoryPort, domainEventPublisherPort);
    }
}
