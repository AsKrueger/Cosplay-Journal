package com.cosplayjournal.application.dto;

public record ImportEventsResult(
        int totalFound,
        int created,
        int updated,
        int skipped,
        int failed
) {
}
