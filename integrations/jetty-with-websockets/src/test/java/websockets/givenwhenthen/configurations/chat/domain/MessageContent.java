package websockets.givenwhenthen.configurations.chat.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageContent {
    private final String value;

    public static MessageContent messageContent(final String value) {
        return new MessageContent(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
