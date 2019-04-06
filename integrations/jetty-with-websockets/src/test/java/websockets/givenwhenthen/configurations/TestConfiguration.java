package websockets.givenwhenthen.configurations;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.websockets.WebSocketModule;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestConfiguration {
    private final HttpMate httpMate;
    private final WebSocketModule webSocketModule;

    public static TestConfiguration testConfiguration(final HttpMate httpMate,
                                                      final WebSocketModule webSocketModule) {
        validateNotNull(httpMate, "httpMate");
        validateNotNull(webSocketModule, "webSocketModule");
        return new TestConfiguration(httpMate, webSocketModule);
    }

    public HttpMate httpMate() {
        return httpMate;
    }

    public WebSocketModule webSocketModule() {
        return webSocketModule;
    }
}
