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

import com.envimate.httpmate.HttpMate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import websockets.givenwhenthen.configurations.TestConfiguration;

import static com.envimate.httpmate.jettywithwebsockets.JettyEndpointWithWebSocketsSupport.jettyEndpointWithWebSocketsSupportFor;
import static websockets.givenwhenthen.FreePortPool.freePort;
import static websockets.givenwhenthen.ReportBuilder.reportBuilder;
import static websockets.givenwhenthen.configurations.TestConfiguration.testConfiguration;
import static websockets.givenwhenthen.configurations.artificial.usecases.WaitableObject.resetAllWaitableObjects;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Given {
    private final TestConfiguration testConfiguration;

    public static Given given(final HttpMate httpMate) {
        final TestConfiguration testConfiguration = testConfiguration(httpMate);
        return given(testConfiguration);
    }

    public static Given given(final TestConfiguration testConfiguration) {
        resetAllWaitableObjects();
        return new Given(testConfiguration);
    }

    public When when() {
        final int port = freePort();
        jettyEndpointWithWebSocketsSupportFor(testConfiguration.httpMate()).listeningOnThePort(port);
        return new When(reportBuilder(), port, testConfiguration);
    }
}
