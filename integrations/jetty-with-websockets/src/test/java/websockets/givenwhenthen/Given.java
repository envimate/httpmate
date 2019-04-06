package websockets.givenwhenthen;

import com.envimate.httpmate.jettywithwebsockets.JettyEndpointWithWebSocketsSupport;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import websockets.givenwhenthen.configurations.TestConfiguration;

import static websockets.givenwhenthen.FreePortPool.freePort;
import static websockets.givenwhenthen.ReportBuilder.reportBuilder;
import static websockets.givenwhenthen.configurations.artificial.ArtificialConfiguration.theExampleHttpMateInstanceWithWebSocketsSupport;
import static websockets.givenwhenthen.configurations.artificial.usecases.WaitableObject.resetAllWaitableObjects;
import static websockets.givenwhenthen.configurations.chat.ChatConfiguration.theExampleChatServerHttpMateInstance;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Given {
    private final TestConfiguration testConfiguration;

    public static Given givenTheExampleHttpMateInstanceWithWebSocketSupport() {
        resetAllWaitableObjects();
        return new Given(theExampleHttpMateInstanceWithWebSocketsSupport());
    }

    public static Given givenTheExampleChatServer() {
        return new Given(theExampleChatServerHttpMateInstance());
    }

    public When when() {
        final int port = freePort();
        JettyEndpointWithWebSocketsSupport.jettyEndpointWithWebSocketsSupportFor(testConfiguration.httpMate()).listeningOnThePort(port);
        return new When(reportBuilder(), port, testConfiguration);
    }
}
