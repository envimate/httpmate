package websockets.exampleproject.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageId {
    private final String id;

    static MessageId randomMessageId() {
        return new MessageId(UUID.randomUUID().toString());
    }

    public static MessageId messageId(final String id) {
        validateNotNull(id, "id");
        return new MessageId(id);
    }

    public String stringValue() {
        return id;
    }
}
