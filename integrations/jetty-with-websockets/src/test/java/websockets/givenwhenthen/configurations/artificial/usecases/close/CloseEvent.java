package websockets.givenwhenthen.configurations.artificial.usecases.close;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class CloseEvent {

    public static CloseEvent closeEvent() {
        return new CloseEvent();
    }
}
