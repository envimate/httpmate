package websockets.givenwhenthen.configurations.chat.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Username {
    private final String name;

    public static Username username(final String name) {
        return new Username(name);
    }

    public String internalValueForMapping() {
        return name;
    }
}
