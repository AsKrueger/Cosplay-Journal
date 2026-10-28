package com.cosplayjournal.application.dto;

/** Solicitud de amistad recibida, con el perfil público de quien la envía. */
public record FriendRequestSummary(String id, PublicUserProfile requester) {
}
