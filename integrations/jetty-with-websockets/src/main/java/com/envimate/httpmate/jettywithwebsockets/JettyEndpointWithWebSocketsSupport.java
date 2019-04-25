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

package com.envimate.httpmate.jettywithwebsockets;

import com.envimate.httpmate.HttpMate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;

import static com.envimate.httpmate.servletwithwebsockets.WebSocketAwareHttpMateServlet.webSocketAwareHttpMateServlet;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JettyEndpointWithWebSocketsSupport implements AutoCloseable {
    private final Server server;

    public static PortStage jettyEndpointWithWebSocketsSupportFor(final HttpMate httpMate) {
        return port -> {
            final Server server;
            try {
                server = new Server(port);
                final ServletHandler servletHandler = new ServletHandler();
                server.setHandler(servletHandler);
                final Servlet servlet = webSocketAwareHttpMateServlet(httpMate);
                final ServletHolder servletHolder = new ServletHolder(servlet);
                servletHandler.addServletWithMapping(servletHolder, "/*");
                server.start();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            return new JettyEndpointWithWebSocketsSupport(server);
        };
    }

    @Override
    public void close() {
        try {
            server.stop();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
