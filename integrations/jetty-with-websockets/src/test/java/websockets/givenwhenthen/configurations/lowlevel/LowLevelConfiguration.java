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

package websockets.givenwhenthen.configurations.lowlevel;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.logger.LoggerImplementation;
import websockets.givenwhenthen.configurations.TestConfiguration;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.HttpMateChainKeys.PATH;
import static com.envimate.httpmate.events.EventConfigurators.toEnrichTheIntermediateMapWithAllRequestData;
import static com.envimate.httpmate.logger.LoggerConfigurators.toLogUsing;
import static com.envimate.httpmate.websockets.WebSocketsConfigurator.toUseWebSockets;
import static com.envimate.httpmate.websockets.WebsocketChainKeys.IS_WEBSOCKET_MESSAGE;
import static websockets.givenwhenthen.configurations.TestConfiguration.testConfiguration;
import static websockets.givenwhenthen.configurations.lowlevel.EchoHandler.echoHandler;
import static websockets.givenwhenthen.configurations.lowlevel.FooBarHandler.fooBarHandler;
import static websockets.givenwhenthen.configurations.lowlevel.LoggerHandler.loggerHandler;

public final class LowLevelConfiguration {
    public static StringBuilder logger;

    private LowLevelConfiguration() {
    }

    public static TestConfiguration theLowLevelHttpMateInstanceWithWebSocketsSupport() {
        logger = new StringBuilder();
        final HttpMate httpMate = anHttpMate()
                .serving(fooBarHandler()).when(metaData -> metaData.get(PATH).matches("/foobar"))
                .serving(loggerHandler()).when(metaData -> metaData.get(PATH).matches("/logger"))
                .serving(echoHandler()).when(metaData -> metaData.getOptional(IS_WEBSOCKET_MESSAGE).orElse(false))
                .configured(toUseWebSockets()
                        .acceptingWebSocketsToThePath("/").taggedBy("ROOT")
                        .acceptingWebSocketsToThePath("/foobar").taggedBy("FOOBAR")
                        .acceptingWebSocketsToThePath("/logger").taggedBy("LOGGER"))
                .configured(toLogUsing(logger()))
                .configured(toEnrichTheIntermediateMapWithAllRequestData())
                .build();
        return testConfiguration(httpMate);
    }

    private static LoggerImplementation logger() {
        return logMessage -> {
            final String formattedMessage = logMessage.formattedMessage();
            logger.append(formattedMessage);
        };
    }
}
