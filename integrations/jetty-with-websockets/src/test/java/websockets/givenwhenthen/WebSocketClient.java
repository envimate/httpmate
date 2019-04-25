/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
import static javax.websocket.ClientEndpointConfig.Builder.create;
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
        final ClientEndpointConfig clientEndpointConfig = create().configurator(configurator).build();
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
