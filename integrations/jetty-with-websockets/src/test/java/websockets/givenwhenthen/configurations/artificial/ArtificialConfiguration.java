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

package websockets.givenwhenthen.configurations.artificial;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.path.Path;
import com.envimate.httpmate.security.SecurityConfigurators;
import com.envimate.httpmate.websockets.registry.WebSocketRegistry;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapter;
import com.google.gson.Gson;
import websockets.givenwhenthen.configurations.TestConfiguration;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseA;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseB;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseC;
import websockets.givenwhenthen.configurations.artificial.usecases.both.BothUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.close.CloseUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.count.CountUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCaseParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.normal.NormalUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.query.QueryParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.query.QueryUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.queryfoo.QueryFooUseCase;

import java.util.Map;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.HttpMateChainKeys.REQUEST_BODY_MAP;
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
import static com.envimate.httpmate.websockets.WebsocketChainKeys.WEBSOCKET_REGISTRY;
import static com.envimate.httpmate.websocketsevents.Conditions.webSocketIsTaggedWith;
import static com.envimate.messageMate.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.processingContext.EventType.eventTypeFromString;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvocationBuilder.anUseCaseAdapter;
import static websockets.givenwhenthen.configurations.TestConfiguration.testConfiguration;
import static websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoParameter.echoParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCaseParameter.exceptionUseCaseParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderParameter.headerParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterParameter.parameterParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.query.QueryParameter.queryParameter;

public final class ArtificialConfiguration {
    private static final int POOL_SIZE = 4;
    public static volatile MessageBus messageBus;

    private ArtificialConfiguration() {
    }

    @SuppressWarnings("unchecked")
    public static TestConfiguration theExampleHttpMateInstanceWithWebSocketsSupport() {
        messageBus = aMessageBus()
                .forType(MessageBusType.ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(POOL_SIZE))
                .build();
        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(NormalUseCase.class).forType("NormalUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(BothUseCase.class).forType("BothUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(CountUseCase.class).forType("CountUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(CloseUseCase.class).forType("CloseUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(QueryFooUseCase.class).forType("QueryFooUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(UseCaseA.class).forType("UseCaseA").callingTheSingleUseCaseMethod()
                .invokingUseCase(UseCaseB.class).forType("UseCaseB").callingTheSingleUseCaseMethod()
                .invokingUseCase(UseCaseC.class).forType("UseCaseC").callingTheSingleUseCaseMethod()
                .invokingUseCase(ExceptionUseCase.class).forType("ExceptionUseCaseParameter").callingTheSingleUseCaseMethod()
                .invokingUseCase(QueryUseCase.class).forType("QueryParameter").callingTheSingleUseCaseMethod()
                .invokingUseCase(HeaderUseCase.class).forType("HeaderParameter").callingTheSingleUseCaseMethod()
                .invokingUseCase(ParameterUseCase.class).forType("ParameterParameter").callingTheSingleUseCaseMethod()
                .invokingUseCase(EchoUseCase.class).forType("EchoParameter").callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .mappingRequestsToUseCaseParametersOfType(QueryParameter.class).using((targetType, map) -> queryParameter((String) map.get("var")))
                .mappingRequestsToUseCaseParametersOfType(HeaderParameter.class).using((targetType, map) -> headerParameter((String) map.get("var")))
                .mappingRequestsToUseCaseParametersOfType(ParameterParameter.class).using((targetType, map) -> parameterParameter((String) map.get("var")))
                .mappingRequestsToUseCaseParametersOfType(EchoParameter.class).using((targetType, map) -> echoParameter((String) map.get("echoValue")))
                .mappingRequestsToUseCaseParametersOfType(ExceptionUseCaseParameter.class).using((targetType, map) -> exceptionUseCaseParameter((String) map.get("mode")))
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied()
                .serializingResponseObjectsOfType(String.class).using(object -> Map.of("stringValue", object))
                .throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault()
                .buildAsStandaloneAdapter();

        useCaseAdapter.attachAndEnhance(messageBus);

        final HttpMate httpMate = anHttpMate()
                .disableAutodectectionOfModules()
                .get("/normal", eventTypeFromString("NormalUseCase"))
                .get("/both", eventTypeFromString("BothUseCase"))
                .serving(eventTypeFromString("CloseUseCase")).when(webSocketIsTaggedWith("CLOSE"))
                .serving(eventTypeFromString("CountUseCase")).when(webSocketIsTaggedWith("COUNT"))
                .serving(eventTypeFromString("UseCaseA")).when(metaData -> metaData.get(REQUEST_BODY_MAP).getOrDefault("useCase", "").equals("A"))
                .serving(eventTypeFromString("UseCaseB")).when(metaData -> metaData.get(REQUEST_BODY_MAP).getOrDefault("useCase", "").equals("B"))
                .serving(eventTypeFromString("UseCaseC")).when(metaData -> metaData.get(REQUEST_BODY_MAP).getOrDefault("useCase", "").equals("C"))
                .serving(eventTypeFromString("QueryFooUseCase")).when(webSocketIsTaggedWith("QUERY_FOO"))
                .serving(eventTypeFromString("ExceptionUseCaseParameter")).when(webSocketIsTaggedWith("EXCEPTION"))
                .serving(eventTypeFromString("EchoParameter")).when(webSocketIsTaggedWith("ECHO"))
                .serving(eventTypeFromString("ParameterParameter")).when(webSocketIsTaggedWith("PARAMETERIZED"))
                .serving(eventTypeFromString("QueryParameter")).when(webSocketIsTaggedWith("QUERY"))
                .serving(eventTypeFromString("HeaderParameter")).when(webSocketIsTaggedWith("HEADER"))

                .configured(toUseTheMessageBus(messageBus))

                .configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(json()).with(string -> new Gson().fromJson(string, Map.class))
                        .marshallingContentTypeInResponses(json()).with(map -> new Gson().toJson(map))
                        .usingTheDefaultContentType(json()))

                .configured(toAuthenticateRequestsUsing(request -> request.queryParameters().getOptionalQueryParameter("username")).notFailingOnMissingAuthentication())
                .configured(toAuthenticateRequestsUsing(request -> request.headers().getOptionalHeader("username")).notFailingOnMissingAuthentication())
                .configured(SecurityConfigurators.toAuthorizeRequestsUsing((authenticationInformation, request) -> {
                    final Path path = request.path();
                    if (path.matches("/authorized")) {
                        return authenticationInformation
                                .map("admin"::equals)
                                .orElse(false);
                    }
                    return true;
                }))
                .configured(toLogUsing(stderrLogger()))
                .configured(toUseWebSockets()
                        .acceptingWebSocketsToThePath("/").taggedBy("ROOT")
                        .acceptingWebSocketsToThePath("/close").taggedBy("CLOSE")
                        .acceptingWebSocketsToThePath("/both").taggedBy("BOTH")
                        .acceptingWebSocketsToThePath("/authorized").taggedBy("AUTHORIZED")
                        .acceptingWebSocketsToThePath("/count").taggedBy("COUNT")
                        .acceptingWebSocketsToThePath("/query_foo").taggedBy("QUERY_FOO")
                        .acceptingWebSocketsToThePath("/echo").taggedBy("ECHO")
                        .acceptingWebSocketsToThePath("/pre/<var>/post").taggedBy("PARAMETERIZED")
                        .acceptingWebSocketsToThePath("/query").taggedBy("QUERY")
                        .acceptingWebSocketsToThePath("/header").taggedBy("HEADER")
                        .acceptingWebSocketsToThePath("/exception").taggedBy("EXCEPTION"))

                .configured(toEnrichTheIntermediateMapWithAllRequestData())
                .configured(toUseModules(eventModule()))
                .build();

        final WebSocketRegistry webSocketRegistry = httpMate.getMetaDatum(WEBSOCKET_REGISTRY);
        messageBus.subscribe(eventTypeFromString("CloseEvent"),
                o -> webSocketRegistry.allActiveWebSockets().forEach(webSocket -> {
                    webSocketRegistry.unregister(webSocket.id());
                    webSocket.close();
                }));

        return testConfiguration(httpMate);
    }
}
