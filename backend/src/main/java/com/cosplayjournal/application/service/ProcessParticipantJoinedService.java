package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.ProcessParticipantJoinedCommand;
import com.cosplayjournal.application.port.in.ProcessParticipantJoinedUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessParticipantJoinedService implements ProcessParticipantJoinedUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessParticipantJoinedService.class);
    private final Map<String, ProcessParticipantJoinedCommand> processedActivityStore = new ConcurrentHashMap<>();

    @Override
    public void processParticipantJoined(ProcessParticipantJoinedCommand command) {
        String key = command.participationId() + ":" + command.userId();

        if (processedActivityStore.containsKey(key)) {
            log.info("Acción asíncrona ignorada por idempotencia (ya procesada previamente): {}", key);
            return;
        }

        processedActivityStore.put(key, command);
        log.info("Procesando actividad asíncrona -> Usuario '{}' se unió a la participación '{}' con rol '{}'",
                command.userId(), command.participationId(), command.role());
    }

    public boolean isProcessed(String participationId, String userId) {
        return processedActivityStore.containsKey(participationId + ":" + userId);
    }
}
