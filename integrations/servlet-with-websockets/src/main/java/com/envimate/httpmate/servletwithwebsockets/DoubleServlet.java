package com.envimate.httpmate.servletwithwebsockets;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.websockets.registry.WebSocketId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.envimate.httpmate.chains.HttpMateChains.PRE_PROCESS;
import static com.envimate.httpmate.servlet.ServletHandling.extractMetaDataFromHttpServletRequest;
import static com.envimate.httpmate.servlet.ServletHandling.handle;
import static com.envimate.httpmate.servletwithwebsockets.JettyStyleWebSocket.jettyStyleSocket;
import static com.envimate.httpmate.websockets.WEBSOCKET_CHAIN_KEYS.*;
import static com.envimate.httpmate.websockets.registry.WebSocketId.randomWebSocketId;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DoubleServlet extends WebSocketServlet {
    private final HttpMate httpMate;

    public static DoubleServlet doubleServletFor(final HttpMate httpMate) {
        return new DoubleServlet(httpMate);
    }

    @Override
    public void configure(final WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setCreator((servletUpgradeRequest, servletUpgradeResponse) -> {
            final MetaData metaData = extractMetaDataFromHttpServletRequest(servletUpgradeRequest.getHttpServletRequest());
            final WebSocketId webSocketId = randomWebSocketId();
            metaData.set(WEBSOCKET_ID, webSocketId);
            final JettyStyleWebSocket jettyStyleSocket = jettyStyleSocket(httpMate, webSocketId);
            metaData.set(WEBSOCKET_DELEGATE, jettyStyleSocket);

            httpMate.handle(PRE_PROCESS, metaData);

            if(metaData.getOptional(WEBSOCKET_ACCEPTED).orElse(false)) {
                return jettyStyleSocket;
            } else {
                return null;
            }
        });
    }

    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws IOException {
        handle(httpMate, request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response) throws IOException {
        handle(httpMate, request, response);
    }

    @Override
    protected void doPut(final HttpServletRequest request,
                         final HttpServletResponse response) throws IOException {
        handle(httpMate, request, response);
    }

    @Override
    protected void doDelete(final HttpServletRequest request,
                            final HttpServletResponse response) throws IOException {
        handle(httpMate, request, response);
    }

    @Override
    protected void doOptions(final HttpServletRequest request,
                             final HttpServletResponse response) throws IOException {
        handle(httpMate, request, response);
    }
}
