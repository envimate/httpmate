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

package websockets.givenwhenthen.configurations.chat;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.security.Authenticator;
import com.envimate.httpmate.security.Authorizer;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.useCaseAdapter.UseCaseAdapter;
import com.google.gson.Gson;
import websockets.givenwhenthen.configurations.TestConfiguration;
import websockets.givenwhenthen.configurations.chat.domain.User;
import websockets.givenwhenthen.configurations.chat.domain.UserRepository;
import websockets.givenwhenthen.configurations.chat.domain.Username;
import websockets.givenwhenthen.configurations.chat.usecases.ChatMessage;
import websockets.givenwhenthen.configurations.chat.usecases.SendMessageUseCase;

import java.util.Map;
import java.util.Objects;

import static com.envimate.httpmate.HttpMate.anHttpMateConfiguredAs;
import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.convenience.configurators.Configurators.toLogUsing;
import static com.envimate.httpmate.events.EventDrivenBuilder.EVENT_DRIVEN;
import static com.envimate.httpmate.http.ContentType.json;
import static com.envimate.httpmate.http.HttpRequestMethod.GET;
import static com.envimate.httpmate.logger.Loggers.stderrLogger;
import static com.envimate.httpmate.security.Configurators.toAuthenticateRequests;
import static com.envimate.httpmate.security.Configurators.toAuthorizeRequests;
import static com.envimate.httpmate.unpacking.BodyMapParsingModule.toParseBodiesBy;
import static com.envimate.httpmate.websockets.WebSocketsConfigurator.toUseWebSockets;
import static com.envimate.httpmate.websocketsevents.Conditions.forwardingItToAllWebSocketsThat;
import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterBuilder.anUseCaseAdapter;
import static websockets.givenwhenthen.configurations.TestConfiguration.testConfiguration;
import static websockets.givenwhenthen.configurations.chat.domain.MessageContent.messageContent;
import static websockets.givenwhenthen.configurations.chat.domain.UserRepository.userRepository;
import static websockets.givenwhenthen.configurations.chat.domain.Username.username;
import static websockets.givenwhenthen.configurations.chat.usecases.ChatMessage.chatMessage;

public final class ChatConfiguration {

    private static final int POOL_SIZE = 4;
    public static volatile MessageBus messageBus;

    private ChatConfiguration() {
    }

    @SuppressWarnings("unchecked")
    public static TestConfiguration theExampleChatServerHttpMateInstance() {
        messageBus = aMessageBus()
                .forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousPipeConfiguration(POOL_SIZE))
                .build();

        final UserRepository userRepository = userRepository();
        final Authenticator authenticator = metaData -> metaData.get(HEADERS)
                .getHeader("user")
                .map(Username::username)
                .map(userRepository::byName);
        final Authorizer authorizer = metaData -> metaData.getOptional(AUTHENTICATION_INFORMATION).isPresent();

        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(SendMessageUseCase.class).forType("ChatMessage")
                .callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .mappingRequestsToUseCaseParametersOfType(ChatMessage.class).using((type, map) -> {
                    final String content = (String) map.get("content");
                    final String recipient = (String) map.get("recipient");
                    return chatMessage(messageContent(content), username(recipient));
                })
                .throwAnExceptionByDefault()
                .serializingResponseObjectsThat(Objects::isNull).using(object -> null)
                .throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();

        useCaseAdapter.attachTo(messageBus);

        final HttpMate httpMate = anHttpMateConfiguredAs(EVENT_DRIVEN).attachedTo(messageBus)
                .triggeringTheEvent("ChatMessage").forRequestPath("/send").andRequestMethod(GET)
                .handlingTheEvent("NewMessageEvent").by(forwardingItToAllWebSocketsThat((metaData, event) -> {
                    final String username = metaData.getAs(AUTHENTICATION_INFORMATION, User.class)
                            .name().internalValueForMapping();
                    return Objects.equals(event.get("recipient"), username);
                }))
                .mappingResponsesUsing((event, metaData) -> {
                    final Map<String, Object> map = (Map<String, Object>) event;
                    final String content = (String) map.get("content");
                    metaData.set(RESPONSE_STRING, content);
                })
                .configured(toAuthenticateRequests().beforeBodyProcessing().using(authenticator))
                .configured(toAuthorizeRequests().beforeBodyProcessing().using(authorizer))
                .configured(toLogUsing(stderrLogger()))
                .configured(toUseWebSockets()
                        .acceptingWebSocketsToThePath("/subscribe").saving(AUTHENTICATION_INFORMATION))
                .configured(toParseBodiesBy()
                        .parsingContentType(json()).with(body -> new Gson().fromJson(body, Map.class))
                        .usingTheDefaultContentType(json()))
                .build();

        return testConfiguration(httpMate);
    }
}
