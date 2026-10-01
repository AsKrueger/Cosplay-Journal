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
    public ProcessParticipantJoinedService processParticipantJoinedService() {
        return new ProcessParticipantJoinedService();
    }

    @Bean
    public UserApplicationService userApplicationService(
            UserRepositoryPort userRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            TokenProviderPort tokenProviderPort
    ) {
        return new UserApplicationService(userRepositoryPort, passwordHasherPort, tokenProviderPort);
    }

    @Bean
    public ImportExternalEventsService importExternalEventsService(
            ExternalEventSourcePort externalEventSourcePort,
            EventRepositoryPort eventRepositoryPort,
            DomainEventPublisherPort domainEventPublisherPort
    ) {
        return new ImportExternalEventsService(externalEventSourcePort, eventRepositoryPort, domainEventPublisherPort);
    }

    @Bean
    public CosplayApplicationService cosplayApplicationService(
            CosplayRepositoryPort cosplayRepositoryPort,
            DomainEventPublisherPort domainEventPublisherPort,
            CurrentUserPort currentUserPort
    ) {
        return new CosplayApplicationService(cosplayRepositoryPort, domainEventPublisherPort, currentUserPort);
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
            CurrentUserPort currentUserPort,
            ParticipationValidationDomainService validationDomainService
    ) {
        return new ParticipationApplicationService(
                participationRepositoryPort,
                eventRepositoryPort,
                cosplayRepositoryPort,
                domainEventPublisherPort,
                currentUserPort,
                validationDomainService
        );
    }

    @Bean
    public PhotoApplicationService photoApplicationService(
            PhotoRepositoryPort photoRepositoryPort,
            ParticipationRepositoryPort participationRepositoryPort,
            DomainEventPublisherPort domainEventPublisherPort,
            CurrentUserPort currentUserPort
    ) {
        return new PhotoApplicationService(photoRepositoryPort, participationRepositoryPort, domainEventPublisherPort, currentUserPort);
    }
}
