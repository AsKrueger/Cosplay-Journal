package com.cosplayjournal.application.port.in;

public record CreateCosplayCommand(
        String name,
        String description,
        String characterName,
        String originSeries
) {
    public CreateCosplayCommand {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cosplay es obligatorio");
        }
    }
}
