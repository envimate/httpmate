package websockets.givenwhenthen.configurations.artificial.usecases.exception;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UseCaseException extends RuntimeException {

    public static UseCaseException useCaseException() {
        return new UseCaseException();
    }
}
