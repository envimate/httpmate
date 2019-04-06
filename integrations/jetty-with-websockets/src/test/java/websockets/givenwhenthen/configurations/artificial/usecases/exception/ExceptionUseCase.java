package websockets.givenwhenthen.configurations.artificial.usecases.exception;

import static websockets.givenwhenthen.configurations.artificial.usecases.exception.UseCaseException.useCaseException;

public final class ExceptionUseCase {

    public String exceptionUseCase(ExceptionUseCaseParameter parameter) {
        if (parameter.isInThrowExceptionMode()) {
            throw useCaseException();
        } else {
            return "hello";
        }
    }
}
