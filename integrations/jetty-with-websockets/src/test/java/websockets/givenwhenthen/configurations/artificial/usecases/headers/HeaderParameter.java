package websockets.givenwhenthen.configurations.artificial.usecases.headers;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderParameter {
    private final String value;

    public static HeaderParameter headerParameter(final String value) {
        return new HeaderParameter(value);
    }

    public String value() {
        return value;
    }
}
