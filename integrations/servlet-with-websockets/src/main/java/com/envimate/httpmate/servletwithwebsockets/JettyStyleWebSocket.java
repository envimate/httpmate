package com.envimate.httpmate.servletwithwebsockets;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.websockets.WebSocketDelegate;
import com.envimate.httpmate.websockets.registry.WebSocketId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;

import static com.envimate.httpmate.chains.HttpMateChainKeys.BODY_STRING;
import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.websockets.WEBSOCKET_CHAINS.*;
import static com.envimate.httpmate.websockets.WEBSOCKET_CHAIN_KEYS.IS_WEBSOCKET_MESSAGE;
import static com.envimate.httpmate.websockets.WEBSOCKET_CHAIN_KEYS.WEBSOCKET_ID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JettyStyleWebSocket implements WebSocketListener, WebSocketDelegate {
    private final HttpMate httpMate;
    private final WebSocketId id;
    private Session session;

    static JettyStyleWebSocket jettyStyleSocket(final HttpMate httpMate,
                                                final WebSocketId webSocketId) {
        validateNotNull(httpMate, "httpMate");
        validateNotNull(webSocketId, "webSocketId");
        return new JettyStyleWebSocket(httpMate, webSocketId);
    }

    @Override
    public void sendText(final String text) {
        final RemoteEndpoint remote = session.getRemote();
        try {
            remote.sendString(text);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.session.close();
    }

    @Override
    public void onWebSocketBinary(final byte[] bytes, final int i, final int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onWebSocketText(final String text) {
        final MetaData metaData = emptyMetaData();
        metaData.set(WEBSOCKET_ID, id);
        metaData.set(BODY_STRING, text);
        metaData.set(IS_WEBSOCKET_MESSAGE, true);
        httpMate.handle(WEBSOCKET_MESSAGE, metaData);
    }

    @Override
    public void onWebSocketClose(final int i, final String s) {
        final MetaData metaData = emptyMetaData();
        metaData.set(WEBSOCKET_ID, id);
        httpMate.handle(WEBSOCKET_CLOSED, metaData);
    }

    @Override
    public void onWebSocketConnect(final Session session) {
        this.session = session;
        final MetaData metaData = emptyMetaData();
        metaData.set(WEBSOCKET_ID, id);
        httpMate.handle(WEBSOCKET_OPEN, metaData);
    }

    @Override
    public void onWebSocketError(final Throwable throwable) {
    }
}
