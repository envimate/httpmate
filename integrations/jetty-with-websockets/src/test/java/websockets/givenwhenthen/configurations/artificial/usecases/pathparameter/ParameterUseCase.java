package websockets.givenwhenthen.configurations.artificial.usecases.pathparameter;

public final class ParameterUseCase {

    public String run(final ParameterParameter parameterParameter) {
        return parameterParameter.value();
    }
}
