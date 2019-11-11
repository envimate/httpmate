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

package com.envimate.httpmate.tests.givenwhenthen;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

import static com.envimate.httpmate.tests.givenwhenthen.Request.request;

final class Server {
    private static final int OK = 200;

    private Server() {
    }

    static void start(final int port, final RequestLog requestLog) {
        final HttpServer httpServer;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/", exchange -> {
                final URI requestURI = exchange.getRequestURI();
                final Request request = request(requestURI.getRawPath());
                requestLog.log(request);
                exchange.sendResponseHeaders(OK, 0);
                exchange.close();
            });
            httpServer.setExecutor(null);
            httpServer.start();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
