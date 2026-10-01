package com.cosplayjournal.application.dto;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public PageResult {
        if (content == null) {
            content = List.of();
        }
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), page, size, 0L, 0, true);
    }
}
