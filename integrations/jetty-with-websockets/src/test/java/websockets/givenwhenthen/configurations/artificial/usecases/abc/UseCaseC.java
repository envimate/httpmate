package websockets.givenwhenthen.configurations.artificial.usecases.abc;

import websockets.givenwhenthen.configurations.artificial.usecases.WaitableObject;

import static websockets.givenwhenthen.configurations.artificial.usecases.WaitableObject.waitableObjectWithInitialValue;

public final class UseCaseC {
    public static WaitableObject<Boolean> HAS_BEEN_INVOKED = waitableObjectWithInitialValue(false);

    public void useCaseC() {
        HAS_BEEN_INVOKED.modify(oldValue -> true);
    }
}
