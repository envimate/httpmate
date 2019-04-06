/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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
import com.envimate.httpmate.convenience.Http;
import com.envimate.httpmate.convenience.preprocessors.NotAuthorizedException;
import com.envimate.httpmate.event.NoEventTypeMappingForWebserviceRequestException;
import com.envimate.httpmate.multipart.MultipartIteratorBody;
import com.envimate.httpmate.tests.usecases.authorized.AuthorizedUseCase;
import com.envimate.httpmate.tests.usecases.download.DownloadUseCase;
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
import com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions.DataTransferObject;
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
import com.envimate.messageMate.messageBus.EventType;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.envimate.httpmate.HttpMate.aHttpMateInstance;
import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.convenience.Http.StatusCodes.METHOD_NOT_ALLOWED;
import static com.envimate.httpmate.convenience.Http.StatusCodes.OK;
import static com.envimate.httpmate.convenience.cors.CorsModule.corsModule;
import static com.envimate.httpmate.mapmate.MapMateIntegration.theMapMateDeserializerOnTheRequestBody;
import static com.envimate.httpmate.mapmate.MapMateIntegration.theMapMateSerializer;
import static com.envimate.httpmate.multipart.MULTIPART_CHAIN_KEYS.MULTIPART_ITERATOR_BODY;
import static com.envimate.httpmate.multipart.MultipartModule.multipartModule;
import static com.envimate.httpmate.request.ContentType.json;
import static com.envimate.httpmate.request.HttpRequestMethod.*;
import static com.envimate.httpmate.unpacking.BodyMapParsingModule.aBodyMapParsingModule;
import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.allBut;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument;
import static com.envimate.messageMate.messageBus.EventType.eventTypeFromString;
import static java.util.Arrays.asList;
import static java.util.Map.of;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public final class HttpMateTestConfigurations {

    private static final Deserializer DESERIALIZER = aDeserializer()
            .thatScansThePackage("com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions")
            .forCustomPrimitives().filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
            .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
            .thatScansThePackage("com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions")
            .forDataTransferObjects().filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
            .thatAre().deserializedUsingTheSingleFactoryMethod()
            .withUnmarshaller(new Gson()::fromJson)
            .build();

    private static final Serializer SERIALIZER = Serializer.aSerializer()
            .thatScansThePackage("com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions")
            .forCustomPrimitives().filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
            .thatAre().serializedUsingTheMethodNamed("map")
            .thatScansThePackage("com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions")
            .forDataTransferObjects().filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
            .thatAre().serializedByItsPublicFields()
            .withMarshaller(new Gson()::toJson)
            .build();

    private HttpMateTestConfigurations() {
    }

    public static HttpMate theHttpMateInstanceUsedForTesting() {
        final HttpMate httpMate = aHttpMateInstance()
                .servingTheUseCase(TestUseCase.class).forRequestPath("/test").andRequestMethods(GET, POST, PUT, DELETE)
                .servingTheUseCase(EchoBodyUseCase.class).forRequestPath("/echo_body").andRequestMethods(GET, POST, PUT, DELETE)
                .servingTheUseCase(MappedExceptionUseCase.class).forRequestPath("/mapped_exception").andRequestMethod(GET)
                .servingTheUseCase(UnmappedExceptionUseCase.class).forRequestPath("/unmapped_exception").andRequestMethod(GET)
                .servingTheUseCase(WildCardUseCase.class).forRequestPath("/wild/<parameter>/card").andRequestMethod(GET)
                .servingTheUseCase(ParameterizedUseCase.class).forRequestPath("/parameterized").andRequestMethod(GET)
                .servingTheUseCase(DownloadUseCase.class).forRequestPath("download").andRequestMethod(GET)
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
                .mappingEventsToUseCaseParametersOfType(Parameter.class).using((targetType, map) -> new Parameter())
                .mappingEventsToUseCaseParametersOfType(WildcardParameter.class).using((targetType, map) -> new WildcardParameter((String) map.get("parameter")))
                .mappingEventsToUseCaseParametersOfType(QueryParametersParameter.class).using((targetType, map) -> new QueryParametersParameter((Map<String, String>) (Object) map))
                .mappingEventsToUseCaseParametersOfType(HeadersParameter.class).using((targetType, map) -> new HeadersParameter((Map<String, String>) (Object) map))
                /*
                .mappingRequestsToUseCaseParametersOfType(EchoContentTypeValue.class).using((targetType, metaData) -> {
                    final ContentType contentType = metaData.get(HTTP_MATE_CHAIN_KEYS.CONTENT_TYPE);
                    return new EchoContentTypeValue(contentType.internalValueForMapping());
                })
                .mappingRequestsToUseCaseParametersOfType(EchoBodyValue.class).using((targetType, metaData) -> new EchoBodyValue(metaData.get(BODY_STRING)))
                .mappingRequestsToUseCaseParametersOfType(EchoMultipartValue.class).using((targetType, metaData) -> echoMultipartValue(metaData.get(MULTIPART_ITERATOR_BODY)))
                .mappingRequestsToUseCaseParametersOfType(MultipartPart.class).using((targetType, metaData) -> metaData.get(MULTIPART_ITERATOR_BODY).next())
                */
                .mappingEventsToUseCaseParametersOfType(EchoPathAndQueryParametersValue.class).using((targetType, map) -> new EchoPathAndQueryParametersValue((Map<String, String>) (Object) map))
                .mappingEventsToUseCaseParametersOfType(EchoAuthenticationInformationValue.class).using((targetType, map) -> {
                    final String username = (String) map.getOrDefault("username", "guest");
                    return new EchoAuthenticationInformationValue(username);
                })
                .mappingEventsToUseCaseParametersOfType(Parameter1.class).using(((targetType, map) -> {
                    final Object param1 = map.get("param1");
                    return new Parameter1((String) param1);
                }))
                .mappingEventsToUseCaseParametersOfType(Parameter2.class).using(((targetType, map) -> {
                    final Object param2 = map.get("param2");
                    return new Parameter2((String) param2);
                }))
                .mappingEventsToUseCaseParametersByDefaultUsing(theMapMateDeserializerOnTheRequestBody(DESERIALIZER)/*.andInjectingRequestValuesIntoTheJsonBodyUsing((metaData, json) -> {
                    final Map<String, String> dtoMap;
                    if(json.containsKey("dataTransferObject")) {
                        dtoMap = (Map<String, String>) json.get("dataTransferObject");
                    } else {
                        dtoMap = new HashMap<>();
                        json.put("dataTransferObject", dtoMap);
                    }
                    metaData.get(HEADERS).asStringMap().forEach(dtoMap::put);
                    metaData.get(PATH_PARAMETERS).asStringMap().forEach(dtoMap::put);
                    metaData.get(QUERY_PARAMETERS).asStringMap().forEach(dtoMap::put);
                })*/)
                .mappingRequestsToEventByDirectlyMappingAllData()

                //.serializingResponseObjectsOfType(Download.class).using(theDownloadSerializer())
                .serializingResponseObjectsOfType(HeadersInResponseReturnValue.class).using(value -> of(value.key, value.value))
                .serializingResponseObjectsOfType(SetContentTypeInResponseValue.class).using(value -> of("contentType", value.value))
                .serializingResponseObjectsOfType(DataTransferObject.class).using(theMapMateSerializer(SERIALIZER))
                .serializingResponseObjectsThat(Objects::isNull).using(object -> null)
                //.serializingResponseObjectsByDefaultUsing(obje)
                .serializingResponseObjectsByDefaultUsing(object -> of("response", object.toString()))
                .mappingEventsToResponsesUsing((event, metaData) -> {
                    metaData.get(EVENT_RETURN_VALUE).ifPresent(responseMap -> {
                        if (responseMap.containsKey("response")) {
                            metaData.set(STRING_RESPONSE, (String) responseMap.get("response"));
                        } else {
                            final String stringResponse = new Gson().toJson(responseMap);
                            metaData.set(STRING_RESPONSE, stringResponse);
                        }
                        if (responseMap.containsKey("foo")) {
                            metaData.get(RESPONSE_HEADERS).put("foo", (String) responseMap.get("foo"));
                        }
                        if (responseMap.containsKey("contentType")) {
                            metaData.get(RESPONSE_HEADERS).put(Http.Headers.CONTENT_TYPE, (String) responseMap.get("contentType"));
                        }
                    });
                })
                .configuredBy((configurator, useCaseConfigurator) -> {
                    configurator.configureExceptionMapping().mappingExceptionsOfType(NoEventTypeMappingForWebserviceRequestException.class).using((exception, metaData) -> {
                        metaData.set(RESPONSE_STATUS, METHOD_NOT_ALLOWED);
                        metaData.set(STRING_RESPONSE, "No use case found.");
                    });
                    configurator.configureExceptionMapping().mappingExceptionsOfType(MappedException.class).using((exception, metaData) -> metaData.set(RESPONSE_STATUS, 201));
                    configurator.configureExceptionMapping().mappingExceptionsOfType(NotAuthorizedException.class).using((exception, metaData) -> {
                        metaData.set(RESPONSE_STATUS, 403);
                        metaData.set(STRING_RESPONSE, "Go away.");
                    });
                    configurator.configureResponseTemplate().usingTheResponseTemplate(metaData -> {
                        metaData.set(RESPONSE_STATUS, OK);
                        metaData.get(RESPONSE_HEADERS).put(Http.Headers.CONTENT_TYPE, "application/json");
                    });
                    configurator.configureSecurity().addAuthenticator(metaData -> metaData.getOptional(BODY_MAP).map(map -> map.get("username")));
                    configurator.configureSecurity().addAuthenticator(metaData -> metaData.get(HEADERS).getHeader("username"));
                    configurator.configureSecurity().addAuthenticator(metaData -> metaData.get(QUERY_PARAMETERS).getQueryParameter("username"));
                    configurator.configureSecurity().addAuthenticator(metaData -> {
                        final EventType eventType = metaData.get(EVENT_TYPE);
                        if (!(eventType.equals(eventTypeFromString("com.envimate.httpmate.tests.usecases.authorized.AuthorizedUseCase")) || eventType.equals(eventTypeFromString("com.envimate.httpmate.tests.usecases.echoauthentication.EchoAuthenticationInformationUseCase")))) {
                            return empty();
                        }
                        final Optional<MultipartIteratorBody> multipartIteratorBody = metaData.getOptional(MULTIPART_ITERATOR_BODY);
                        return multipartIteratorBody.map(iterator -> {
                            final String content = iterator.next().readContentToString();
                            return extractUsername(content).orElse(null);
                        });
                    });
                    configurator.configureSecurity().addAuthenticator(metaData -> metaData.getOptional(BODY_STRING).map(body -> extractUsername(body).orElse(null)));
                    configurator.configureSecurity().addAuthorizer(metaData -> {
                        final EventType eventType = metaData.get(EVENT_TYPE);
                        if (!eventType.equals(eventTypeFromString("com.envimate.httpmate.tests.usecases.authorized.AuthorizedUseCase"))) {
                            return true;
                        }
                        return metaData.getOptional(AUTHENTICATION_INFORMATION).map("admin"::equals).orElse(false);
                    });
                    configurator.configureLogger().loggingToStderr();
                    configurator.registerModule(corsModule("*", asList(GET, POST, PUT, DELETE), asList("X-Custom-Header", "Upgrade-Insecure-Requests")));
                    configurator.registerModule(multipartModule());
                    configurator.registerModule(aBodyMapParsingModule()
                            .parsingContentType(json()).with(body -> new Gson().fromJson(body, Map.class))
                            .usingTheDefaultContentType(json()));
                });

        return httpMate;
    }

    private static Optional<String> extractUsername(final String keyValue) {
        if (keyValue.startsWith("username=")) {
            return of(keyValue.substring(9));
        }
        return empty();
    }
}
