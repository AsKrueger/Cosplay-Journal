package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidFriendshipException;
import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FriendshipTest {

    private final UserId ana = UserId.of("ana");
    private final UserId luis = UserId.of("luis");

    @Test
    @DisplayName("Nadie puede enviarse una solicitud a sí mismo")
    void cannotRequestYourself() {
        assertThrows(InvalidFriendshipException.class, () -> Friendship.request(ana, ana));
    }

    @Test
    @DisplayName("Solo el destinatario puede aceptar, y solo una vez")
    void onlyAddresseeAcceptsOnce() {
        Friendship friendship = Friendship.request(ana, luis);

        assertThrows(InvalidFriendshipException.class, () -> friendship.accept(ana));

        friendship.accept(luis);
        assertTrue(friendship.isAccepted());
        assertThrows(InvalidFriendshipException.class, () -> friendship.accept(luis));
    }

    @Test
    @DisplayName("El otro miembro se calcula desde cualquiera de los dos lados")
    void otherPartyFromEitherSide() {
        Friendship friendship = Friendship.request(ana, luis);

        assertEquals(luis, friendship.otherParty(ana));
        assertEquals(ana, friendship.otherParty(luis));
        assertThrows(InvalidFriendshipException.class, () -> friendship.otherParty(UserId.of("otra")));
    }
}
