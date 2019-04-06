package websockets.givenwhenthen.configurations.chat.usecases;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import websockets.givenwhenthen.configurations.chat.domain.MessageContent;
import websockets.givenwhenthen.configurations.chat.domain.User;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewMessageEvent {
    private final User recipient;
    private final MessageContent content;

    public static NewMessageEvent newMessageEvent(final User recipient,
                                                  final MessageContent content) {
        return new NewMessageEvent(recipient, content);
    }

    public User recipient() {
        return recipient;
    }

    public MessageContent content() {
        return content;
    }

    @Override
    public String toString() {
        return content.toString();
    }
}
