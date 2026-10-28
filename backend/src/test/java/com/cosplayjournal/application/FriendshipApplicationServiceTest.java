package com.cosplayjournal.application;

import com.cosplayjournal.application.dto.PublicUserProfile;
import com.cosplayjournal.application.port.out.FriendshipRepositoryPort;
import com.cosplayjournal.application.port.out.UserRepositoryPort;
import com.cosplayjournal.application.service.FriendshipApplicationService;
import com.cosplayjournal.domain.exception.InvalidFriendshipException;
import com.cosplayjournal.domain.exception.UserNotFoundException;
import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipApplicationServiceTest {

    @Mock
    private FriendshipRepositoryPort friendshipRepository;
    @Mock
    private UserRepositoryPort userRepository;

    private FriendshipApplicationService service;
    private final UserId ana = UserId.of("ana");
    private final UserId luis = UserId.of("luis");

    @BeforeEach
    void setUp() {
        service = new FriendshipApplicationService(friendshipRepository, userRepository);
    }

    @Test
    @DisplayName("No se puede enviar una solicitud si ya existe relación en cualquier sentido")
    void duplicateRelationIsRejected() {
        User luisUser = User.create(luis, "luis_99", "luis@example.com", "hash");
        when(userRepository.findByUsername("luis_99")).thenReturn(Optional.of(luisUser));
        when(friendshipRepository.findBetween(ana, luis)).thenReturn(Optional.of(Friendship.request(luis, ana)));

        assertThrows(InvalidFriendshipException.class, () -> service.sendRequest(ana, "luis_99"));
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    @DisplayName("Enviar a un nickname inexistente lanza UserNotFoundException")
    void unknownNickname() {
        when(userRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.sendRequest(ana, "nadie"));
    }

    @Test
    @DisplayName("La lista de amigos solo incluye relaciones aceptadas y no expone email")
    void friendsListOnlyAccepted() {
        Friendship accepted = Friendship.request(ana, luis);
        accepted.accept(luis);
        Friendship pending = Friendship.request(ana, UserId.of("pedro"));
        when(friendshipRepository.findAllInvolving(ana)).thenReturn(List.of(accepted, pending));
        User luisUser = User.create(luis, "luis_99", "luis@example.com", "hash");
        when(userRepository.findById(luis)).thenReturn(Optional.of(luisUser));

        List<PublicUserProfile> friends = service.listFriends(ana);

        assertEquals(1, friends.size());
        assertEquals("luis_99", friends.get(0).nickname());
        assertFalse(friends.get(0).toString().contains("luis@example.com"));
    }

    @Test
    @DisplayName("El solicitante no puede rechazar su propia solicitud")
    void requesterCannotReject() {
        Friendship pending = Friendship.request(ana, luis);
        when(friendshipRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThrows(InvalidFriendshipException.class, () -> service.reject(ana, pending.getId()));
        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Una solicitud ajena responde como si no existiera")
    void foreignRequestIsHidden() {
        Friendship pending = Friendship.request(ana, luis);
        when(friendshipRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThrows(com.cosplayjournal.domain.exception.FriendshipNotFoundException.class,
                () -> service.accept(UserId.of("tercero"), pending.getId()));
    }
}
