package websockets.givenwhenthen.configurations.artificial.usecases.headers;

public final class HeaderUseCase {

    public String run(final HeaderParameter headerParameter) {
        return headerParameter.value();
    }
}
