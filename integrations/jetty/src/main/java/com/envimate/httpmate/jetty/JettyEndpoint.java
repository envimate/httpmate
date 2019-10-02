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

package com.envimate.httpmate.jetty;

import com.envimate.httpmate.HttpMate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;

import static com.envimate.httpmate.jetty.JettyEndpointHandler.jettyEndpointHandler;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JettyEndpoint implements AutoCloseable {
    private final Server server;

    public static PortStage jettyEndpointFor(final HttpMate httpMate) {
        return port -> {
            final Server server;
            try {
                server = new Server(port);
                final HttpConnectionFactory connectionFactory = extractConnectionFactory(server);
                connectionFactory.getHttpConfiguration().setFormEncodedMethods();
                server.setHandler(jettyEndpointHandler(httpMate));
                server.start();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            return new JettyEndpoint(server);
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

    private static HttpConnectionFactory extractConnectionFactory(final Server server) {
        final Connector[] connectors = server.getConnectors();
        if (connectors.length != 1) {
            throw new UnsupportedOperationException("Jetty does not behave as expected");
        }
        final Connector connector = connectors[0];
        final ConnectionFactory connectionFactory = connector.getDefaultConnectionFactory();
        if (!(connectionFactory instanceof HttpConnectionFactory)) {
            throw new UnsupportedOperationException("Jetty does not behave as expected");
        }
        return (HttpConnectionFactory) connectionFactory;
    }
}
