package websockets.givenwhenthen.configurations.artificial.usecases.query;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameter {
    private final String value;

    public static QueryParameter queryParameter(final String value) {
        return new QueryParameter(value);
    }

    public String value() {
        return value;
    }
}
