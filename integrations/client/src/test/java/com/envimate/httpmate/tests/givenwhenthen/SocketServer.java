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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static com.envimate.httpmate.tests.givenwhenthen.Request.request;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SocketServer {
    private final int port;
    private final RequestLog requestLog;

    static void start(final int port, final RequestLog requestLog) {
        final SocketServer socketServer = new SocketServer(port, requestLog);
        new Thread(socketServer::run).start();
    }

    private void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handle(clientSocket);
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handle(final Socket socket) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));

        final String firstLine = reader.readLine();
        requireNonNull(firstLine);
        final String[] tokens = firstLine.split(" ");
        final String url = tokens[1];

        final String host = "http://localhost:" + port;
        final String path = url.replaceAll(host, "");

        requestLog.log(request(path));

        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, UTF_8);
        writer.println("HTTP/1.1 200 OK\n");
    }
}
