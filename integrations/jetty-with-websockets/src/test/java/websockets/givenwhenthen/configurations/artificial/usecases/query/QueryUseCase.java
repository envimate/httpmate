package websockets.givenwhenthen.configurations.artificial.usecases.query;

public final class QueryUseCase {

    public String run(final QueryParameter queryParameter) {
        return queryParameter.value();
    }
}
