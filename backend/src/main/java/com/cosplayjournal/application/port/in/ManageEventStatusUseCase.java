package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.social.UserEventStatus;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;

public interface ManageEventStatusUseCase {
    List<UserEventStatus> listStatuses(UserId userId);
    /** Guarda el estado; "NONE" quita la selección del evento. */
    void setStatus(UserId userId, String eventId, String status);
}
