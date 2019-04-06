package websockets.exampleproject.usecases.events;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import websockets.exampleproject.domain.Message;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewMessageEvent {
    public final Message message;

    public static NewMessageEvent newMessageEvent(final Message message) {
        validateNotNull(message, "message");
        return new NewMessageEvent(message);
    }
}
