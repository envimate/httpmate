package websockets.givenwhenthen;

import com.envimate.httpmate.client.HttpMateClient;
import com.envimate.httpmate.client.requestbuilder.HeadersAndQueryParametersAndMappingStage;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import websockets.givenwhenthen.builder.HeadersStage;
import websockets.givenwhenthen.configurations.TestConfiguration;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseA;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseB;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.envimate.httpmate.client.HttpClientRequest.aGetRequest;
import static com.envimate.httpmate.client.HttpMateClient.aHttpMateClientForTheHost;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.Arrays.stream;
import static websockets.givenwhenthen.WebSocketClient.connectWebSocket;
import static websockets.givenwhenthen.configurations.artificial.usecases.count.CountUseCase.COUNTER;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class When {
    private final ReportBuilder reportBuilder;
    private final int port;
    private final TestConfiguration testConfiguration;
    private final List<WebSocketClient> clients = new LinkedList<>();

    private final Semaphore receptionLock = new Semaphore(0);
    private final Semaphore closeLock = new Semaphore(0);

    public HeadersStage aWebSocketIsConnectedToThePath(final String path) {
        return headers -> {
            final Map<String, String> headersMap = headersToMap(headers);
            try {
                final SingleWebSocketReportBuilder singleWebSocketReportBuilder = reportBuilder.reportNewWebSocket();
                final WebSocketClient webSocketClient = connectWebSocket(format("ws://localhost:%d%s", port, path), headersMap, singleWebSocketReportBuilder, receptionLock, closeLock);
                clients.add(webSocketClient);
            } catch (final Exception e) {
                reportBuilder.reportExceptionExceptionDuringWebSocketConnecting(e);
            }
            return whenOrThen();
        };
    }

    public WhenOrThen aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent() {
        return aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("");
    }

    public WhenOrThen aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent(final String content) {
        mostRecentClient().sendText(content);
        return whenOrThen();
    }

    public WhenOrThen itIsWaitedForTheReceptionOfAFrame() {
        waitMax10SecondsOn(receptionLock);
        return whenOrThen();
    }

    public WhenOrThen itIsWaitedUntilTheCounterOfTheCountUseCaseReaches(final Integer expectedValue) {
        COUNTER.waitUntilReaches(expectedValue::equals);
        return whenOrThen();
    }

    public WhenOrThen itIsWaitedUntilUseCaseAHasBeenInvoked() {
        UseCaseA.HAS_BEEN_INVOKED.waitUntilReaches(value -> value);
        return whenOrThen();
    }

    public WhenOrThen itIsWaitedUntilUseCaseBHasBeenInvoked() {
        UseCaseB.HAS_BEEN_INVOKED.waitUntilReaches(value -> value);
        return whenOrThen();
    }

    public WhenOrThen itItWaitedForTheClosingOfAWebSocket() {
        waitMax10SecondsOn(closeLock);
        return whenOrThen();
    }

    public HeadersStage aNormalGETRequestIsIssuedToThePath(final String path) {
        return headers -> {
            final HttpMateClient httpMateClient = aHttpMateClientForTheHost("localhost")
                    .withThePort(port)
                    .viaHttp()
                    .withoutABasePath()
                    .mappingResponseObjectsToStrings();
            final HeadersAndQueryParametersAndMappingStage builder = aGetRequest().toThePath(path).withoutABody();
            headersToMap(headers).forEach(builder::withHeader);
            final String response = httpMateClient.issue(builder.mappedTo(String.class));
            reportBuilder.reportNormalResponseBody(response);
            return whenOrThen();
        };
    }

    public WhenOrThen allWebSocketsAreClosedOnClientSide() {
        clients.forEach(WebSocketClient::close);
        return whenOrThen();
    }

    public WhenOrThen theNumberOfActiveWebSocketsIsQueried() {
        final int numberOfActiveWebSockets = testConfiguration.webSocketModule().metrics().numberOfActiveWebSockets();
        reportBuilder.reportNumberOfActiveWebSockets(numberOfActiveWebSockets);
        return whenOrThen();
    }

    private WebSocketClient mostRecentClient() {
        return clients.get(clients.size() - 1);
    }

    private static Map<String, String> headersToMap(final String[] headers) {
        final Map<String, String> headersMap = new HashMap<>();
        stream(headers).forEach(keyValue -> {
            final String[] keyValueArray = keyValue.split("=");
            headersMap.put(keyValueArray[0], keyValueArray[1]);
        });
        return headersMap;
    }

    private WhenOrThen whenOrThen() {
        return WhenOrThen.whenOrThen(this, reportBuilder);
    }

    private static void waitMax10SecondsOn(final Semaphore semaphore) {
        try {
            semaphore.tryAcquire(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
