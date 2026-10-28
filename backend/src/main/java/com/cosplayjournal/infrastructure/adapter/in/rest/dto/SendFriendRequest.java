package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record SendFriendRequest(
        @NotBlank(message = "El nickname es obligatorio")
        String nickname
) {
}
