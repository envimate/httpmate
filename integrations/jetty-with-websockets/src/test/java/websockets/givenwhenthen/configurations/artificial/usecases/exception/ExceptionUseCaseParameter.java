package websockets.givenwhenthen.configurations.artificial.usecases.exception;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionUseCaseParameter {
    private final String mode;

    public static ExceptionUseCaseParameter exceptionUseCaseParameter(final String query) {
        return new ExceptionUseCaseParameter(query);
    }

    boolean isInThrowExceptionMode() {
        return mode.equals("throw");
    }
}
