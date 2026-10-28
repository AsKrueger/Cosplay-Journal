package com.cosplayjournal.application.port.in;

import com.cosplayjournal.application.dto.ConversationView;
import com.cosplayjournal.application.dto.MessageView;
import com.cosplayjournal.domain.model.chat.MessageKind;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.List;

public interface ManageChatsUseCase {
    ConversationView openPrivate(UserId actor, String nickname);
    ConversationView createGroup(UserId actor, String name, List<String> nicknames);
    List<ConversationView> listConversations(UserId actor);
    List<MessageView> listMessages(UserId actor, String conversationId, Instant since);
    MessageView send(UserId actor, String conversationId, MessageKind kind, String body);
}
