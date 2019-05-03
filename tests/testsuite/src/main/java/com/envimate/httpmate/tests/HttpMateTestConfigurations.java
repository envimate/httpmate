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

package com.envimate.httpmate.tests;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.handler.NoHandlerFoundException;
import com.envimate.httpmate.http.Http;
import com.envimate.httpmate.security.NotAuthorizedException;
import com.envimate.httpmate.tests.usecases.ToStringWrapper;
import com.envimate.httpmate.tests.usecases.authorized.AuthorizedUseCase;
import com.envimate.httpmate.tests.usecases.echoauthentication.EchoAuthenticationInformationUseCase;
import com.envimate.httpmate.tests.usecases.echoauthentication.EchoAuthenticationInformationValue;
import com.envimate.httpmate.tests.usecases.echobody.EchoBodyUseCase;
import com.envimate.httpmate.tests.usecases.echocontenttype.EchoContentTypeUseCase;
import com.envimate.httpmate.tests.usecases.echomultipart.EchoMultipartUseCase;
import com.envimate.httpmate.tests.usecases.echopathandqueryparameters.EchoPathAndQueryParametersUseCase;
import com.envimate.httpmate.tests.usecases.echopathandqueryparameters.EchoPathAndQueryParametersValue;
import com.envimate.httpmate.tests.usecases.headers.HeaderUseCase;
import com.envimate.httpmate.tests.usecases.headers.HeadersParameter;
import com.envimate.httpmate.tests.usecases.mapmate.MapMateUseCase;
import com.envimate.httpmate.tests.usecases.mappedexception.MappedException;
import com.envimate.httpmate.tests.usecases.mappedexception.MappedExceptionUseCase;
import com.envimate.httpmate.tests.usecases.multipartandmapmate.MultipartAndMapmateUseCase;
import com.envimate.httpmate.tests.usecases.parameter.Parameter;
import com.envimate.httpmate.tests.usecases.parameter.ParameterizedUseCase;
import com.envimate.httpmate.tests.usecases.pathparameter.WildCardUseCase;
import com.envimate.httpmate.tests.usecases.pathparameter.WildcardParameter;
import com.envimate.httpmate.tests.usecases.queryparameters.QueryParametersParameter;
import com.envimate.httpmate.tests.usecases.queryparameters.QueryParametersUseCase;
import com.envimate.httpmate.tests.usecases.responsecontenttype.SetContentTypeInResponseUseCase;
import com.envimate.httpmate.tests.usecases.responsecontenttype.SetContentTypeInResponseValue;
import com.envimate.httpmate.tests.usecases.responseheaders.HeadersInResponseReturnValue;
import com.envimate.httpmate.tests.usecases.responseheaders.HeadersInResponseUseCase;
import com.envimate.httpmate.tests.usecases.simple.TestUseCase;
import com.envimate.httpmate.tests.usecases.twoparameters.Parameter1;
import com.envimate.httpmate.tests.usecases.twoparameters.Parameter2;
import com.envimate.httpmate.tests.usecases.twoparameters.TwoParametersUseCase;
import com.envimate.httpmate.tests.usecases.unmappedexception.UnmappedExceptionUseCase;
import com.envimate.httpmate.tests.usecases.vooooid.VoidUseCase;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.serialization.Serializer;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

import static com.envimate.httpmate.HttpMate.anHttpMateConfiguredAs;
import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.convenience.configurators.Configurators.toCustomizeResponsesUsing;
import static com.envimate.httpmate.convenience.configurators.Configurators.toLogUsing;
import static com.envimate.httpmate.convenience.configurators.exceptions.ExceptionMappingConfigurator.toMapExceptions;
import static com.envimate.httpmate.convenience.cors.CorsConfigurator.toProtectAjaxRequestsAgainstCsrfAttacksByTellingTheBrowserThatRequests;
import static com.envimate.httpmate.events.EventModule.EVENT_TYPE;
import static com.envimate.httpmate.events.EventsChains.MAP_REQUEST_TO_EVENT;
import static com.envimate.httpmate.exceptions.DefaultExceptionMapper.theDefaultExceptionMapper;
import static com.envimate.httpmate.http.ContentType.json;
import static com.envimate.httpmate.http.Http.StatusCodes.METHOD_NOT_ALLOWED;
import static com.envimate.httpmate.http.Http.StatusCodes.OK;
import static com.envimate.httpmate.http.HttpRequestMethod.*;
import static com.envimate.httpmate.logger.Loggers.stderrLogger;
import static com.envimate.httpmate.mapmate.MapMateSerializerAndDeserializer.mapMate;
import static com.envimate.httpmate.security.Configurators.toAuthenticateRequests;
import static com.envimate.httpmate.security.Configurators.toAuthorizeRequests;
import static com.envimate.httpmate.tests.Util.extractUsername;
import static com.envimate.httpmate.usecases.UseCaseDrivenBuilder.USE_CASE_DRIVEN;
import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.allBut;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument;
import static com.envimate.messageMate.messageBus.EventType.eventTypeFromString;
import static java.util.Map.of;

public final class HttpMateTestConfigurations {

    private static final Deserializer DESERIALIZER = aDeserializer()
            .thatScansThePackage("com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions")
            .forCustomPrimitives().filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
            .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
            .thatScansThePackage("com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions")
            .forDataTransferObjects().filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
            .thatAre().deserializedUsingTheSingleFactoryMethod()
            .withJsonUnmarshaller(new Gson()::fromJson)
            .build();

    private static final Serializer SERIALIZER = Serializer.aSerializer()
            .thatScansThePackage("com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions")
            .forCustomPrimitives().filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
            .thatAre().serializedUsingTheMethodNamed("map")
            .thatScansThePackage("com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions")
            .forDataTransferObjects().filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
            .thatAre().serializedByItsPublicFields()
            .withJsonMarshaller(new Gson()::toJson)
            .build();

    private HttpMateTestConfigurations() {
    }

    @SuppressWarnings("unchecked")
    public static HttpMate theHttpMateInstanceUsedForTesting() {
        final HttpMate httpMate = anHttpMateConfiguredAs(USE_CASE_DRIVEN)
                .servingTheUseCase(TestUseCase.class).forRequestPath("/test").andRequestMethods(GET, POST, PUT, DELETE)
                .servingTheUseCase(EchoBodyUseCase.class).forRequestPath("/echo_body").andRequestMethods(GET, POST, PUT, DELETE)
                .servingTheUseCase(MappedExceptionUseCase.class).forRequestPath("/mapped_exception").andRequestMethod(GET)
                .servingTheUseCase(UnmappedExceptionUseCase.class).forRequestPath("/unmapped_exception").andRequestMethod(GET)
                .servingTheUseCase(WildCardUseCase.class).forRequestPath("/wild/<parameter>/card").andRequestMethod(GET)
                .servingTheUseCase(ParameterizedUseCase.class).forRequestPath("/parameterized").andRequestMethod(GET)
                .servingTheUseCase(QueryParametersUseCase.class).forRequestPath("/queryparameters").andRequestMethod(GET)
                .servingTheUseCase(HeaderUseCase.class).forRequestPath("/headers").andRequestMethod(GET)
                .servingTheUseCase(HeadersInResponseUseCase.class).forRequestPath("/headers_response").andRequestMethod(GET)
                .servingTheUseCase(EchoContentTypeUseCase.class).forRequestPath("/echo_contenttype").andRequestMethod(GET)
                .servingTheUseCase(SetContentTypeInResponseUseCase.class).forRequestPath("/set_contenttype_in_response").andRequestMethod(GET)
                .servingTheUseCase(EchoMultipartUseCase.class).forRequestPath("/multipart_echo").andRequestMethods(GET, POST, PUT, DELETE)
                .servingTheUseCase(EchoAuthenticationInformationUseCase.class).forRequestPath("/authentication_echo").andRequestMethods(GET, POST)
                .servingTheUseCase(AuthorizedUseCase.class).forRequestPath("/authorized").andRequestMethod(POST)
                .servingTheUseCase(MapMateUseCase.class).forRequestPath("/mapmate/<value1>").andRequestMethods(GET, POST)
                .servingTheUseCase(EchoPathAndQueryParametersUseCase.class).forRequestPath("/echo_path_and_query_parameters/<wildcard>").andRequestMethod(GET)
                .servingTheUseCase(TwoParametersUseCase.class).forRequestPath("/twoparameters").andRequestMethod(GET)
                .servingTheUseCase(VoidUseCase.class).forRequestPath("/void").andRequestMethod(GET)
                .servingTheUseCase(MultipartAndMapmateUseCase.class).forRequestPath("/multipart_and_mapmate").andRequestMethod(PUT)
                .mappingUseCaseParametersOfType(Parameter.class).using((targetType, map) -> new Parameter())
                .mappingUseCaseParametersOfType(WildcardParameter.class).using((targetType, map) -> new WildcardParameter((String) map.get("parameter")))
                .mappingUseCaseParametersOfType(QueryParametersParameter.class).using((targetType, map) -> new QueryParametersParameter((Map<String, String>) (Object) map))
                .mappingUseCaseParametersOfType(HeadersParameter.class).using((targetType, map) -> new HeadersParameter((Map<String, String>) (Object) map))
                .mappingUseCaseParametersOfType(EchoPathAndQueryParametersValue.class).using((targetType, map) -> new EchoPathAndQueryParametersValue((Map<String, String>) (Object) map))
                .mappingUseCaseParametersOfType(EchoAuthenticationInformationValue.class).using((targetType, map) -> {
                    final String username = (String) map.getOrDefault("username", "guest");
                    return new EchoAuthenticationInformationValue(username);
                })
                .mappingUseCaseParametersOfType(Parameter1.class).using(((targetType, map) -> {
                    final Object param1 = map.get("param1");
                    return new Parameter1((String) param1);
                }))
                .mappingUseCaseParametersOfType(Parameter2.class).using(((targetType, map) -> {
                    final Object param2 = map.get("param2");
                    return new Parameter2((String) param2);
                }))
                .serializingResponseObjectsOfType(HeadersInResponseReturnValue.class).using(value -> of(value.key, value.value))
                .serializingResponseObjectsOfType(SetContentTypeInResponseValue.class).using(value -> of("contentType", value.value))
                .serializingResponseObjectsThat(Objects::isNull).using(object -> null)
                .serializingResponseObjectsOfType(String.class).using(string -> of("response", string))
                .serializingResponseObjectsOfType(ToStringWrapper.class).using(wrapper -> of("response", wrapper.toString()))
                .mappingRequestsAndResponsesUsing(mapMate()
                        .mappingAllStandardContentTypes()
                        .assumingTheDefaultContentType(json())
                        .bySerializingUsing(SERIALIZER)
                        .andDeserializingUsing(DESERIALIZER))

                .configured(toMapExceptions()
                        .ofType(NoHandlerFoundException.class)
                        .toResponsesUsing((exception, metaData) -> {
                            metaData.set(RESPONSE_STATUS, METHOD_NOT_ALLOWED);
                            metaData.set(RESPONSE_STRING, "No use case found.");
                        })
                        .ofType(MappedException.class).toResponsesUsing((exception, metaData) -> metaData.set(RESPONSE_STATUS, 201))
                        .ofType(NotAuthorizedException.class).toResponsesUsing((object, metaData) -> {
                            metaData.set(RESPONSE_STATUS, 403);
                            metaData.set(RESPONSE_STRING, "Go away.");
                        })
                        .ofAllRemainingTypesUsing(theDefaultExceptionMapper())
                )

                .configured(toCustomizeResponsesUsing(metaData -> {
                    metaData.set(RESPONSE_STATUS, OK);
                    metaData.get(RESPONSE_HEADERS).put(Http.Headers.CONTENT_TYPE, "application/json");
                }))

                .configured(toProtectAjaxRequestsAgainstCsrfAttacksByTellingTheBrowserThatRequests()
                        .usingTheHttpMethods(GET, POST, PUT, DELETE)
                        .shouldOnlyOriginateFromSitesHostedOn("*")
                        .andOnlyContainTheHeaders("X-Custom-Header", "Upgrade-Insecure-Requests"))

                .configured(toLogUsing(stderrLogger()))

                .configured(toAuthenticateRequests().afterBodyProcessing().using(metaData -> metaData.getOptional(BODY_MAP).map(map -> map.get("username"))))
                .configured(toAuthenticateRequests().beforeBodyProcessing().using(metaData -> metaData.get(HEADERS).getHeader("username")))
                .configured(toAuthenticateRequests().beforeBodyProcessing().using(metaData -> metaData.get(QUERY_PARAMETERS).getQueryParameter("username")))
                .configured(toAuthenticateRequests().afterBodyProcessing().using(metaData -> metaData.getOptional(BODY_STRING).map(body -> extractUsername(body).orElse(null))))
                .configured(toAuthorizeRequests().inPhase(MAP_REQUEST_TO_EVENT).using(metaData -> metaData.getOptional(EVENT_TYPE).map(eventType -> {
                    if (!eventType.equals(eventTypeFromString("com.envimate.httpmate.tests.usecases.authorized.AuthorizedUseCase"))) {
                        return true;
                    }
                    return metaData.getOptional(AUTHENTICATION_INFORMATION).map("admin"::equals).orElse(false);
                }).orElse(true)))
                .build();

        System.out.println(httpMate.dumpChains());

        return httpMate;
    }
}
