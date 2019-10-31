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
import com.envimate.httpmate.events.EventModule;
import com.envimate.httpmate.handler.http.HttpRequest;
import com.envimate.httpmate.security.SecurityConfigurators;
import com.envimate.httpmate.security.authentication.Authenticator;
import com.envimate.httpmate.security.authorization.HttpAuthorizer;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapter;
import com.google.gson.Gson;
import websockets.givenwhenthen.configurations.TestConfiguration;
import websockets.givenwhenthen.configurations.chat.domain.User;
import websockets.givenwhenthen.configurations.chat.domain.UserRepository;
import websockets.givenwhenthen.configurations.chat.domain.Username;
import websockets.givenwhenthen.configurations.chat.usecases.ChatMessage;
import websockets.givenwhenthen.configurations.chat.usecases.SendMessageUseCase;

import java.util.Map;
import java.util.Objects;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.HttpMateChainKeys.AUTHENTICATION_INFORMATION;
import static com.envimate.httpmate.chains.Configurator.configuratorForType;
import static com.envimate.httpmate.chains.Configurator.toUseModules;
import static com.envimate.httpmate.events.EventConfigurators.toEnrichTheIntermediateMapWithAllRequestData;
import static com.envimate.httpmate.events.EventConfigurators.toUseTheMessageBus;
import static com.envimate.httpmate.events.EventModule.eventModule;
import static com.envimate.httpmate.http.headers.ContentType.json;
import static com.envimate.httpmate.logger.LoggerConfigurators.toLogUsing;
import static com.envimate.httpmate.logger.Loggers.stderrLogger;
import static com.envimate.httpmate.marshalling.MarshallingModule.toMarshallBodiesBy;
import static com.envimate.httpmate.security.SecurityConfigurators.toAuthenticateRequestsUsing;
import static com.envimate.httpmate.websockets.WebSocketsConfigurator.toUseWebSockets;
import static com.envimate.httpmate.websocketsevents.Conditions.forwardingItToAllWebSocketsThat;
import static com.envimate.messageMate.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.processingContext.EventType.eventTypeFromString;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvocationBuilder.anUseCaseAdapter;
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
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(POOL_SIZE))
                .build();

        final UserRepository userRepository = userRepository();
        final Authenticator<HttpRequest> authenticator = request -> request.headers()
                .getOptionalHeader("user")
                .map(Username::username)
                .map(userRepository::byName);
        final HttpAuthorizer authorizer = (authenticationInformation, request) -> authenticationInformation.isPresent();

        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(SendMessageUseCase.class).forType("ChatMessage")
                .callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .mappingRequestsToUseCaseParametersOfType(ChatMessage.class).using((type, map) -> {
                    final String content = (String) map.get("content");
                    final String recipient = (String) map.get("recipient");
                    return chatMessage(messageContent(content), username(recipient));
                })
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied()
                .serializingResponseObjectsThat(Objects::isNull).using(object -> null)
                .throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault()
                .buildAsStandaloneAdapter();

        useCaseAdapter.attachAndEnhance(messageBus);

        final HttpMate httpMate = anHttpMate()
                .get("/send", eventTypeFromString("ChatMessage"))
                .configured(toUseModules(eventModule()))
                .configured(toAuthenticateRequestsUsing(authenticator))
                .configured(SecurityConfigurators.toAuthorizeRequestsUsing(authorizer))
                .configured(toLogUsing(stderrLogger()))
                .configured(toUseWebSockets()
                        .acceptingWebSocketsToThePath("/subscribe").saving(AUTHENTICATION_INFORMATION))
                .configured(toUseTheMessageBus(messageBus))

                .configured(configuratorForType(EventModule.class, eventModule ->
                        eventModule.addExternalEventMapping(eventTypeFromString("NewMessageEvent"),
                                forwardingItToAllWebSocketsThat((metaData, event) -> {
                                    final String username = metaData.getAs(AUTHENTICATION_INFORMATION, User.class)
                                            .name().internalValueForMapping();
                                    return Objects.equals(event.get("recipient"), username);
                                }))))

                .configured(toEnrichTheIntermediateMapWithAllRequestData())

                .configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(json()).with(string -> new Gson().fromJson(string, Map.class))
                        .marshallingContentTypeInResponses(json()).with(map -> new Gson().toJson(map))
                        .usingTheDefaultContentType(json()))
                .build();

        return testConfiguration(httpMate);
    }
}
