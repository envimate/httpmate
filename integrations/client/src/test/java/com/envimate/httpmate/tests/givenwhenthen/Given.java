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

import java.util.function.BiConsumer;

import static com.envimate.httpmate.tests.givenwhenthen.RequestLog.requestLog;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Given {
    private final int port;
    private final RequestLog requestLog;

    public static Given givenAHttpServer() {
        return given(Server::start);
    }

    public static Given givenAnOpenSocketThatCanInterpretHttpValues() {
        return given(SocketServer::start);
    }

    private static Given given(final BiConsumer<Integer, RequestLog> server) {
        final int port = 1337;
        final RequestLog requestLog = requestLog();
        server.accept(port, requestLog);
        return new Given(port, requestLog);
    }

    public When when() {
        return When.when(port, requestLog);
    }
}
