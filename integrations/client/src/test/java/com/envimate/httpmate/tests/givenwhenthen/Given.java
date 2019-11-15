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

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import static com.envimate.httpmate.tests.givenwhenthen.RequestLog.requestLog;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Given {
    private static final int PORT = 1337;
    private static final List<AutoCloseable> CLOSEABLES = new LinkedList<>();

    private final int port;
    private final RequestLog requestLog;

    public static Given givenAnHttpServer() {
        return given(Server::start);
    }

    public static Given givenAnOpenSocketThatCanInterpretHttpValues() {
        return given(SocketServer::start);
    }

    private static Given given(final BiFunction<Integer, RequestLog, AutoCloseable> server) {
        cleanUp();
        final int port = port();
        final RequestLog requestLog = requestLog();
        final AutoCloseable autoCloseable = server.apply(port, requestLog);
        CLOSEABLES.add(autoCloseable);
        return new Given(port, requestLog);
    }

    public When when() {
        return When.when(port, requestLog);
    }

    private static int port() {
        return PORT;
    }

    private static void cleanUp() {
        if (CLOSEABLES.isEmpty()) {
            return;
        }
        final AutoCloseable closeable = CLOSEABLES.remove(0);
        try {
            closeable.close();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        cleanUp();
    }
}
