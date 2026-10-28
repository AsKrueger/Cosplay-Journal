package com.cosplayjournal.application;

import com.cosplayjournal.application.dto.ConversationView;
import com.cosplayjournal.application.port.out.ConversationRepositoryPort;
import com.cosplayjournal.application.port.out.FriendshipRepositoryPort;
import com.cosplayjournal.application.port.out.MessageRepositoryPort;
import com.cosplayjournal.application.port.out.UserRepositoryPort;
import com.cosplayjournal.application.service.ChatApplicationService;
import com.cosplayjournal.domain.exception.ConversationNotFoundException;
import com.cosplayjournal.domain.exception.InvalidChatDataException;
import com.cosplayjournal.domain.model.chat.Conversation;
import com.cosplayjournal.domain.model.chat.MessageKind;
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
class ChatApplicationServiceTest {

    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private MessageRepositoryPort messages;
    @Mock
    private UserRepositoryPort users;
    @Mock
    private FriendshipRepositoryPort friendships;

    private ChatApplicationService service;
    private final UserId ana = UserId.of("ana");
    private final UserId luis = UserId.of("luis");

    @BeforeEach
    void setUp() {
        service = new ChatApplicationService(conversations, messages, users, friendships);
    }

    @Test
    @DisplayName("Solo se puede abrir un chat con un amigo aceptado")
    void privateChatNeedsAcceptedFriendship() {
        when(users.findByUsername("luis_99")).thenReturn(Optional.of(User.create(luis, "luis_99", "l@example.com", "h")));
        when(friendships.findBetween(ana, luis)).thenReturn(Optional.of(Friendship.request(ana, luis)));

        assertThrows(InvalidChatDataException.class, () -> service.openPrivate(ana, "luis_99"));
        verify(conversations, never()).save(any());
    }

    @Test
    @DisplayName("Abrir un chat privado con un amigo crea la conversación")
    void privateChatWithFriend() {
        Friendship accepted = Friendship.request(ana, luis);
        accepted.accept(luis);
        when(users.findByUsername("luis_99")).thenReturn(Optional.of(User.create(luis, "luis_99", "l@example.com", "h")));
        when(friendships.findBetween(ana, luis)).thenReturn(Optional.of(accepted));
        when(conversations.findAllByMember(ana)).thenReturn(List.of());
        when(conversations.save(any(Conversation.class))).thenAnswer(i -> i.getArgument(0));
        when(users.findById(ana)).thenReturn(Optional.of(User.create(ana, "ana_1", "a@example.com", "h")));
        when(users.findById(luis)).thenReturn(Optional.of(User.create(luis, "luis_99", "l@example.com", "h")));

        ConversationView view = service.openPrivate(ana, "luis_99");

        assertEquals("PRIVATE", view.type());
        assertTrue(view.memberNicknames().containsAll(List.of("ana_1", "luis_99")));
    }

    @Test
    @DisplayName("Un usuario que no es miembro no puede leer ni escribir en la conversación")
    void nonMemberIsRejected() {
        Conversation chat = Conversation.privateBetween(ana, luis);
        when(conversations.findById(chat.getId())).thenReturn(Optional.of(chat));

        assertThrows(ConversationNotFoundException.class,
                () -> service.listMessages(UserId.of("intruso"), chat.getId(), null));
        assertThrows(ConversationNotFoundException.class,
                () -> service.send(UserId.of("intruso"), chat.getId(), MessageKind.TEXT, "hola"));
        verify(messages, never()).save(any());
    }
}
