package websockets.givenwhenthen.configurations.artificial.usecases.echo;

public final class EchoUseCase {

    public String echo(final EchoParameter echoParameter) {
        return echoParameter.value();
    }
}
