package websockets.givenwhenthen;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.websocket.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static java.net.URI.create;
import static java.util.Collections.singletonList;
import static javax.websocket.ContainerProvider.getWebSocketContainer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketClient extends Endpoint {
    private final Semaphore receptionLock;
    private final Semaphore closeLock;
    private final SingleWebSocketReportBuilder reportBuilder;
    private Session session;

    static WebSocketClient connectWebSocket(final String url,
                                            final Map<String, String> additionalHeaders,
                                            final SingleWebSocketReportBuilder reportBuilder,
                                            final Semaphore receptionLock,
                                            final Semaphore closeLock) throws Exception {
        final WebSocketContainer container = getWebSocketContainer();
        final ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(final Map<String, List<String>> headers) {
                additionalHeaders.forEach((key, value) -> {
                    final List<String> list = singletonList(value);
                    headers.put(key, list);
                });
            }
        };
        final ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().configurator(configurator).build();
        final WebSocketClient webSocketClient = new WebSocketClient(receptionLock, closeLock, reportBuilder);
        container.connectToServer(webSocketClient, clientEndpointConfig, create(url));
        return webSocketClient;
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        session.addMessageHandler(new StringMessageHandler());
        this.session = session;
    }

    @Override
    public void onClose(final Session session, final CloseReason closeReason) {
        reportBuilder.reportClosed();
        closeLock.release();
    }

    void close() {
        try {
            session.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    void sendText(final String text) {
        session.getAsyncRemote().sendText(text);
    }

    private class StringMessageHandler implements MessageHandler.Whole<String> {
        @Override
        public void onMessage(final String message) {
            reportBuilder.reportReceivedFrame(message);
            receptionLock.release();
        }
    }
}
