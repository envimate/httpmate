package websockets.exampleproject.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static websockets.exampleproject.domain.Message.message;
import static websockets.exampleproject.domain.MessageId.randomMessageId;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageRepository {
    private static final Map<MessageId, Message> MESSAGES = new HashMap<>();

    public static MessageRepository messageRepository() {
        return new MessageRepository();
    }

    public Message addMessage(final MessageContent content,
                              final User user,
                              final List<Username> recipients) {
        validateNotNull(content, "content");
        final MessageId id = randomMessageId();
        final Message message = message(id, content, user.username(), recipients);
        MESSAGES.put(id, message);
        return message;
    }
}
