package websockets.givenwhenthen.configurations.artificial.usecases.abc;

import websockets.givenwhenthen.configurations.artificial.usecases.WaitableObject;

import static websockets.givenwhenthen.configurations.artificial.usecases.WaitableObject.waitableObjectWithInitialValue;

public final class UseCaseB {
    public static final WaitableObject<Boolean> HAS_BEEN_INVOKED = waitableObjectWithInitialValue(false);

    public void useCaseB() {
        HAS_BEEN_INVOKED.modify(oldValue -> true);
    }
}
