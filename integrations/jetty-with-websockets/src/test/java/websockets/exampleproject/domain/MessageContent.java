package websockets.exampleproject.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageContent {
    private final String value;

    public static MessageContent messageContent(final String value) {
        validateNotNullNorEmpty(value, "value");
        return new MessageContent(value);
    }

    public String stringValue() {
        return value;
    }
}
