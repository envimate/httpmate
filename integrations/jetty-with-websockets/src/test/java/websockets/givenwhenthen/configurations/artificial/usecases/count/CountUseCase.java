package websockets.givenwhenthen.configurations.artificial.usecases.count;

import websockets.givenwhenthen.configurations.artificial.usecases.WaitableObject;

import static websockets.givenwhenthen.configurations.artificial.usecases.WaitableObject.waitableObjectWithInitialValue;

public final class CountUseCase {
    public static final WaitableObject<Integer> COUNTER = waitableObjectWithInitialValue(0);

    public void count() {
        COUNTER.modify(integer -> integer + 1);
    }
}
