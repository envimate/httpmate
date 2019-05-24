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
import com.envimate.httpmate.http.HttpRequestMethod;
import com.envimate.httpmate.path.Path;
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

import static com.envimate.httpmate.HttpMate.anHttpMateConfiguredAs;
import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.convenience.configurators.Configurators.toLogUsing;
import static com.envimate.httpmate.events.EventDrivenBuilder.EVENT_DRIVEN;
import static com.envimate.httpmate.http.ContentType.json;
import static com.envimate.httpmate.logger.Loggers.stderrLogger;
import static com.envimate.httpmate.security.Configurators.toAuthenticateRequests;
import static com.envimate.httpmate.security.Configurators.toAuthorizeRequests;
import static com.envimate.httpmate.unpacking.BodyMapParsingModule.toParseBodiesBy;
import static com.envimate.httpmate.websockets.WebSocketsConfigurator.toUseWebSockets;
import static com.envimate.httpmate.websocketsevents.Conditions.closingAllWebSocketsThat;
import static com.envimate.httpmate.websocketsevents.Conditions.webSocketIsTaggedWith;
import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
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
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousPipeConfiguration(POOL_SIZE))
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

        final HttpMate httpMate = anHttpMateConfiguredAs(EVENT_DRIVEN).attachedTo(messageBus)
                .triggeringTheEvent("NormalUseCase").forRequestPath("/normal").andRequestMethod(HttpRequestMethod.GET)
                .triggeringTheEvent("BothUseCase").forRequestPath("/both").andRequestMethod(HttpRequestMethod.GET)
                .triggeringTheEvent("CloseUseCase").when(webSocketIsTaggedWith("CLOSE"))
                .triggeringTheEvent("CountUseCase").when(webSocketIsTaggedWith("COUNT"))
                .triggeringTheEvent("UseCaseA").when(metaData -> metaData.get(BODY_MAP).getOrDefault("useCase", "").equals("A"))
                .triggeringTheEvent("UseCaseB").when(metaData -> metaData.get(BODY_MAP).getOrDefault("useCase", "").equals("B"))
                .triggeringTheEvent("UseCaseC").when(metaData -> metaData.get(BODY_MAP).getOrDefault("useCase", "").equals("C"))
                .triggeringTheEvent("QueryFooUseCase").when(webSocketIsTaggedWith("QUERY_FOO"))
                .triggeringTheEvent("ExceptionUseCaseParameter").when(webSocketIsTaggedWith("EXCEPTION"))
                .triggeringTheEvent("EchoParameter").when(webSocketIsTaggedWith("ECHO"))
                .triggeringTheEvent("ParameterParameter").when(webSocketIsTaggedWith("PARAMETERIZED"))
                .triggeringTheEvent("QueryParameter").when(webSocketIsTaggedWith("QUERY"))
                .triggeringTheEvent("HeaderParameter").when(webSocketIsTaggedWith("HEADER"))
                .handlingTheEvent("CloseEvent").by(closingAllWebSocketsThat((metaData, event) -> true))
                .mappingResponsesUsing((event, metaData) -> metaData.set(RESPONSE_STRING, event.toString()))
                .configured(toAuthenticateRequests().beforeBodyProcessing().using(metaData -> metaData.get(QUERY_PARAMETERS).getQueryParameter("username")))
                .configured(toAuthenticateRequests().beforeBodyProcessing().using(metaData -> metaData.get(HEADERS).getHeader("username")))
                .configured(toAuthorizeRequests().beforeBodyProcessing().using(metaData -> {
                    final Path path = metaData.get(PATH);
                    if (path.matches("/authorized")) {
                        return metaData.getOptional(AUTHENTICATION_INFORMATION)
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
                .configured(toParseBodiesBy()
                        .parsingContentType(json()).with(body -> new Gson().fromJson(body, Map.class))
                        .usingTheDefaultContentType(json()))
                .build();

        return testConfiguration(httpMate);
    }
}
