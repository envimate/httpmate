package websockets.givenwhenthen.configurations.artificial.usecases.pathparameter;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterParameter {
    private final String value;

    public static ParameterParameter parameterParameter(final String value) {
        return new ParameterParameter(value);
    }

    public String value() {
        return value;
    }
}
