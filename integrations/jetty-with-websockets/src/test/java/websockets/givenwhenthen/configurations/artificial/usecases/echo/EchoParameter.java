package websockets.givenwhenthen.configurations.artificial.usecases.echo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EchoParameter {
    private final String value;

    public static EchoParameter echoParameter(final String value) {
        return new EchoParameter(value);
    }

    public String value() {
        return value;
    }
}
