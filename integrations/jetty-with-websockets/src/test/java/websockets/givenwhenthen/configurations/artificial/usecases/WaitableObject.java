package websockets.givenwhenthen.configurations.artificial.usecases;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.System.currentTimeMillis;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaitableObject<T> {
    private static final long ONE_SECOND = 1000;
    private static final List<WaitableObject> WAITABLE_OBJECTS = new LinkedList<>();
    private final T initialObject;
    private T object;

    public static void resetAllWaitableObjects() {
        WAITABLE_OBJECTS.forEach(WaitableObject::reset);
    }

    public static <T> WaitableObject<T> waitableObjectWithInitialValue(final T initialValue) {
        final WaitableObject<T> waitableObject = new WaitableObject<>(initialValue);
        waitableObject.reset();
        WAITABLE_OBJECTS.add(waitableObject);
        return waitableObject;
    }

    private synchronized void reset() {
        object = initialObject;
    }

    public synchronized void modify(final Function<T, T> function) {
        object = function.apply(object);
        notifyAll();
    }

    public synchronized void waitUntilReaches(final Predicate<T> predicate) {
        final long startTime = currentTimeMillis();
        while (currentTimeMillis() - startTime < 10 * ONE_SECOND) {
            if (predicate.test(object)) {
                return;
            }
            waitOnThis(ONE_SECOND);
        }
        throw new RuntimeException("wait failed");
    }

    public synchronized T getValue() {
        return object;
    }

    private synchronized void waitOnThis(final long time) {
        try {
            wait(time);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
