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

package websockets.exampleproject;

import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapter;
import com.google.gson.Gson;
import websockets.exampleproject.domain.*;
import websockets.exampleproject.usecases.AntiHateSpeechUseCase;
import websockets.exampleproject.usecases.SendMessageRequest;
import websockets.exampleproject.usecases.SendMessageResponse;
import websockets.exampleproject.usecases.SendMessageUseCase;
import websockets.exampleproject.usecases.events.NewMessageEvent;

import static com.envimate.httpmate.HttpMate.anHttpMateConfiguredAs;
import static com.envimate.httpmate.HttpMateChainKeys.AUTHENTICATION_INFORMATION;
import static com.envimate.httpmate.events.EventDrivenBuilder.EVENT_DRIVEN;
import static com.envimate.httpmate.http.HttpRequestMethod.DELETE;
import static com.envimate.httpmate.websockets.WebSocketsConfigurator.toUseWebSockets;
import static com.envimate.httpmate.websockets.WebsocketChainKeys.IS_WEBSOCKET_MESSAGE;
import static com.envimate.httpmate.websocketsevents.Conditions.forwardingItToAllWebSocketsThat;
import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;
import static com.envimate.messageMate.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvocationBuilder.anUseCaseAdapter;

public final class Application {

    private static final int POOL_SIZE = 4;
    public static final MessageBus MESSAGE_BUS = aMessageBus()
            .forType(MessageBusType.ASYNCHRONOUS)
            .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(POOL_SIZE))
            .build();

    private Application() {
    }

    public static void startApplication() {

        final Gson gson = new Gson();

        aDeserializer()
                .withJsonUnmarshaller(gson::fromJson)
                .withCustomPrimitive(MessageContent.class).deserializedUsingTheStaticMethodWithSingleStringArgument()
                .withCustomPrimitive(Username.class).deserializedUsingTheStaticMethodWithSingleStringArgument()
                .withDataTransferObject(SendMessageRequest.class).deserializedUsingTheSingleFactoryMethod()
                .withCustomPrimitive(User.class).deserializedUsing(new DeserializationCPMethod() {
                    @Override
                    public void verifyCompatibility(final Class<?> targetType) {
                    }

                    @Override
                    public Object deserialize(final String input, final Class<?> targetType) {
                        return VERY_STUPID.get();
                    }
                })
                .build();

        aSerializer()
                .withJsonMarshaller(gson::toJson)
                .withDataTransferObject(SendMessageResponse.class).serializedByItsPublicFields()
                .withDataTransferObject(Message.class).serializedByItsPublicFields()
                .withDataTransferObject(NewMessageEvent.class).serializedByItsPublicFields()
                .withCustomPrimitive(MessageContent.class).serializedUsingTheMethod(MessageContent::stringValue)
                .withCustomPrimitive(MessageId.class).serializedUsingTheMethod(MessageId::stringValue)
                .withCustomPrimitive(Username.class).serializedUsingTheMethod(Username::stringValue)
                .build();

        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(SendMessageUseCase.class).forType("SendMessageRequest").callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied()
                .throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault()
                .buildAsStandaloneAdapter();
        useCaseAdapter.attachAndEnhance(MESSAGE_BUS);

        anHttpMateConfiguredAs(EVENT_DRIVEN).attachedTo(MESSAGE_BUS)
                .triggeringTheEvent("SendMessageRequest").forRequestPath("/qwrefewiflrwefjierwipower").andRequestMethod(DELETE)
                .triggeringTheEvent("SendMessageRequest").when(metaData -> metaData.getOptional(IS_WEBSOCKET_MESSAGE).orElse(false))
                .handlingTheEvent("NewMessageEvent").by(forwardingItToAllWebSocketsThat((metaData, event) -> {
                    //return event.message.recipients.contains(metaData.get(AUTHENTICATION_INFORMATION));
                    throw new UnsupportedOperationException();
                }))
                .handlingTheEvent("BanUserEvent").by(forwardingItToAllWebSocketsThat((metaData, event) -> {
                    //return category.equals(event.username());
                    throw new UnsupportedOperationException();
                }))
                .mappingResponsesUsing((event, metaData) -> {
                })
                .configured(toUseWebSockets().acceptingWebSocketsToThePath("/connect").saving(AUTHENTICATION_INFORMATION))
                .configured(configurator -> {
                    /*
                    configurator.configureSecurity().addAuthenticator(metaData -> metaData.get(HEADERS).getHeader("cookie").flatMap(
                            cookieHeader -> getCookie("username", cookieHeader).flatMap(
                                    username -> getCookie("password", cookieHeader).flatMap(
                                            password -> userRepository.getIfCorrectAuthenticationInformation(username, password)))));
                    configurator.configureSecurity().addAuthorizer(metaData -> metaData.getOptional(AUTHENTICATION_INFORMATION).isPresent());
                    configurator.configureLogger().loggingToStderr();
                     */
                    //configurator.registerModule(webSocketModule);
                    //configurator.registerModule(multipartModule());
                    //configurator.registerModule(aBodyMapParsingModule()
                    //        .parsingContentType(json()).with(body -> new Gson().fromJson(body, Map.class))
                    //        .usingTheDefaultContentType(json()));
                })
                .build();

        //final JettyEndpoint jettyEndpoint = jettyEndpointFor(doubleServletFor(httpMate)).listeningOnThePort(8976);

        AntiHateSpeechUseCase.register();
    }

    private static final ThreadLocal<User> VERY_STUPID = new ThreadLocal<>();

    public static void main(final String[] args) {
        startApplication();
    }
}
