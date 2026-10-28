package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.ManageEventStatusUseCase;
import com.cosplayjournal.application.port.out.UserEventStatusRepositoryPort;
import com.cosplayjournal.domain.model.social.UserEventStatus;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.List;

public class EventStatusApplicationService implements ManageEventStatusUseCase {

    private final UserEventStatusRepositoryPort repositoryPort;

    public EventStatusApplicationService(UserEventStatusRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<UserEventStatus> listStatuses(UserId userId) {
        return repositoryPort.findAllByUser(userId);
    }

    @Override
    public void setStatus(UserId userId, String eventId, String status) {
        if ("NONE".equals(status)) {
            repositoryPort.delete(userId, eventId);
            return;
        }
        repositoryPort.save(new UserEventStatus(userId, eventId, status, Instant.now()));
    }
}
