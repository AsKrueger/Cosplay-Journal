package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.dto.ConversationView;
import com.cosplayjournal.application.dto.MessageView;
import com.cosplayjournal.application.port.in.ManageChatsUseCase;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.domain.exception.InvalidChatDataException;
import com.cosplayjournal.domain.model.chat.MessageKind;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Chats privados y grupos. Los mensajes se consultan con {@code since} para la actualización periódica.
 */
@RestController
@RequestMapping("/api/v1/chats")
@Tag(name = "Chats", description = "Chats privados entre amigos y grupos")
public class ChatController {

    private final ManageChatsUseCase manageChatsUseCase;
    private final CurrentUserPort currentUserPort;

    public ChatController(ManageChatsUseCase manageChatsUseCase, CurrentUserPort currentUserPort) {
        this.manageChatsUseCase = manageChatsUseCase;
        this.currentUserPort = currentUserPort;
    }

    @GetMapping
    @Operation(summary = "Mis conversaciones")
    public ResponseEntity<List<ConversationView>> list() {
        return ResponseEntity.ok(manageChatsUseCase.listConversations(currentUserPort.getRequiredCurrentUserId()));
    }

    @PostMapping("/private")
    @Operation(summary = "Abrir un chat privado con un amigo", description = "Reutiliza el chat existente si ya lo hay")
    public ResponseEntity<ConversationView> openPrivate(@RequestBody OpenPrivateRequest request) {
        ConversationView view = manageChatsUseCase.openPrivate(currentUserPort.getRequiredCurrentUserId(), request.nickname());
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @PostMapping("/groups")
    @Operation(summary = "Crear un grupo", description = "Los miembros deben ser amigos tuyos")
    public ResponseEntity<ConversationView> createGroup(@RequestBody CreateGroupRequest request) {
        if (request.nicknames() == null) {
            throw new InvalidChatDataException("Indica los miembros del grupo");
        }
        ConversationView view = manageChatsUseCase.createGroup(
                currentUserPort.getRequiredCurrentUserId(), request.name(), request.nicknames());
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Mensajes de una conversación", description = "Con since, solo los posteriores a esa fecha")
    public ResponseEntity<List<MessageView>> messages(
            @PathVariable String id,
            @RequestParam(required = false) Instant since) {
        return ResponseEntity.ok(manageChatsUseCase.listMessages(currentUserPort.getRequiredCurrentUserId(), id, since));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Enviar un mensaje de texto o una foto (enlace)")
    public ResponseEntity<MessageView> send(@PathVariable String id, @RequestBody SendMessageRequest request) {
        MessageKind kind = parseKind(request.kind());
        MessageView view = manageChatsUseCase.send(currentUserPort.getRequiredCurrentUserId(), id, kind, request.body());
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    private MessageKind parseKind(String kind) {
        if (kind == null) {
            return MessageKind.TEXT;
        }
        try {
            return MessageKind.valueOf(kind);
        } catch (IllegalArgumentException e) {
            throw new InvalidChatDataException("Tipo de mensaje no válido: " + kind);
        }
    }

    public record OpenPrivateRequest(String nickname) {
    }

    public record CreateGroupRequest(String name, List<String> nicknames) {
    }

    public record SendMessageRequest(String kind, String body) {
    }
}
