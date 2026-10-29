package com.cosplayjournal.application.service;

import com.cosplayjournal.application.dto.ConversationView;
import com.cosplayjournal.application.dto.MessageView;
import com.cosplayjournal.application.port.in.ManageChatsUseCase;
import com.cosplayjournal.application.port.out.ConversationRepositoryPort;
import com.cosplayjournal.application.port.out.FriendshipRepositoryPort;
import com.cosplayjournal.application.port.out.MessageRepositoryPort;
import com.cosplayjournal.application.port.out.UserRepositoryPort;
import com.cosplayjournal.domain.exception.ConversationNotFoundException;
import com.cosplayjournal.domain.exception.InvalidChatDataException;
import com.cosplayjournal.domain.exception.UserNotFoundException;
import com.cosplayjournal.domain.model.chat.Conversation;
import com.cosplayjournal.domain.model.chat.ConversationType;
import com.cosplayjournal.domain.model.chat.Message;
import com.cosplayjournal.domain.model.chat.MessageKind;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Chats privados y grupos. Solo se puede chatear con amigos y solo dentro de las conversaciones propias.
 */
public class ChatApplicationService implements ManageChatsUseCase {

    private final ConversationRepositoryPort conversationRepository;
    private final MessageRepositoryPort messageRepository;
    private final UserRepositoryPort userRepository;
    private final FriendshipRepositoryPort friendshipRepository;

    public ChatApplicationService(ConversationRepositoryPort conversationRepository,
                                  MessageRepositoryPort messageRepository,
                                  UserRepositoryPort userRepository,
                                  FriendshipRepositoryPort friendshipRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Override
    public ConversationView openPrivate(UserId actor, String nickname) {
        UserId other = resolveFriend(actor, nickname);
        Optional<Conversation> existing = conversationRepository.findAllByMember(actor).stream()
                .filter(c -> c.getType() == ConversationType.PRIVATE && c.hasMember(other))
                .findFirst();
        if (existing.isPresent()) {
            return view(existing.get());
        }
        return view(conversationRepository.save(Conversation.privateBetween(actor, other)));
    }

    @Override
    public ConversationView createGroup(UserId actor, String name, List<String> nicknames) {
        Set<UserId> others = new LinkedHashSet<>();
        for (String nickname : nicknames) {
            others.add(resolveFriend(actor, nickname));
        }
        return view(conversationRepository.save(Conversation.group(actor, name, others)));
    }

    @Override
    public List<ConversationView> listConversations(UserId actor) {
        return conversationRepository.findAllByMember(actor).stream().map(this::view).toList();
    }

    @Override
    public List<MessageView> listMessages(UserId actor, String conversationId, Instant since) {
        Conversation conversation = requireMember(actor, conversationId);
        Instant from = since != null ? since : Instant.EPOCH;
        Map<UserId, String> nicknames = new HashMap<>();
        return messageRepository.findByConversationSince(conversation.getId(), from).stream()
                .map(m -> toView(m, nicknames))
                .toList();
    }

    @Override
    public MessageView send(UserId actor, String conversationId, MessageKind kind, String body) {
        Conversation conversation = requireMember(actor, conversationId);
        Message saved = messageRepository.save(new Message(null, conversation.getId(), actor, kind, body, null));
        return toView(saved, new HashMap<>());
    }

    /** Devuelve el id de un usuario, comprobando que es amigo aceptado del actor. */
    private UserId resolveFriend(UserId actor, String nickname) {
        User other = userRepository.findByUsername(nickname)
                .orElseThrow(() -> new UserNotFoundException("No existe ningún usuario con el nickname: " + nickname));
        if (other.getId().equals(actor)) {
            throw new InvalidChatDataException("No puedes chatear contigo mismo");
        }
        boolean friends = friendshipRepository.findBetween(actor, other.getId())
                .map(f -> f.isAccepted())
                .orElse(false);
        if (!friends) {
            throw new InvalidChatDataException("Solo puedes chatear con tus amigos: " + nickname);
        }
        return other.getId();
    }

    private Conversation requireMember(UserId actor, String conversationId) {
        return conversationRepository.findById(conversationId)
                .filter(c -> c.hasMember(actor))
                .orElseThrow(() -> new ConversationNotFoundException("La conversación no existe"));
    }

    private ConversationView view(Conversation conversation) {
        List<String> nicknames = conversation.getMembers().stream()
                .map(id -> nicknameOf(id, new HashMap<>()))
                .toList();
        return new ConversationView(conversation.getId(), conversation.getType().name(), conversation.getName(),
                nicknames, conversation.getCreatedAt());
    }

    private MessageView toView(Message message, Map<UserId, String> nicknames) {
        return new MessageView(message.id(), nicknameOf(message.senderId(), nicknames), message.kind().name(),
                message.body(), message.createdAt());
    }

    private String nicknameOf(UserId userId, Map<UserId, String> cache) {
        return cache.computeIfAbsent(userId, id -> userRepository.findById(id)
                .map(User::getUsername)
                .orElse("usuario eliminado"));
    }
}
