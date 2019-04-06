package websockets.givenwhenthen.configurations.chat.usecases;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import websockets.givenwhenthen.configurations.chat.domain.MessageContent;
import websockets.givenwhenthen.configurations.chat.domain.Username;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatMessage {
    private final MessageContent content;
    private final Username recipient;

    public static ChatMessage chatMessage(final MessageContent content,
                                          final Username recipient) {
        return new ChatMessage(content, recipient);
    }

    public MessageContent content() {
        return content;
    }

    public Username recipient() {
        return recipient;
    }
}
