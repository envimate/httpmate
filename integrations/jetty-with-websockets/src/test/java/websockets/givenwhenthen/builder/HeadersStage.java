package websockets.givenwhenthen.builder;

import websockets.givenwhenthen.WhenOrThen;

public interface HeadersStage {

    default WhenOrThen withoutHeaders() {
        return withTheHeaders();
    }

    WhenOrThen withTheHeaders(final String... headers);
}
