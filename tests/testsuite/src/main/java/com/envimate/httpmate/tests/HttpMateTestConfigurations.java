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
import com.envimate.httpmate.mapmate.MapMateSerializerAndDeserializer;
import com.envimate.httpmate.tests.usecases.ToStringWrapper;
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
import com.envimate.httpmate.usecases.UseCasesModule;
import com.envimate.mapmate.builder.MapMate;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.serialization.Serializer;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

import static com.envimate.httpmate.Configurators.toCustomizeResponsesUsing;
import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_HEADERS;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_STATUS;
import static com.envimate.httpmate.chains.Configurator.configuratorForType;
import static com.envimate.httpmate.events.EventConfigurators.toEnrichTheIntermediateMapWithAllRequestData;
import static com.envimate.httpmate.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static com.envimate.httpmate.http.Http.StatusCodes.METHOD_NOT_ALLOWED;
import static com.envimate.httpmate.http.Http.StatusCodes.OK;
import static com.envimate.httpmate.http.HttpRequestMethod.*;
import static com.envimate.httpmate.logger.LoggerConfigurators.toLogToStderr;
import static com.envimate.httpmate.mapmate.MapMateSerializerAndDeserializer.mapMateIntegration;
import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.allBut;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument;
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

    private static final MapMate MAP_MATE = MapMate.mapMate(SERIALIZER, DESERIALIZER);

    private HttpMateTestConfigurations() {
    }

    @SuppressWarnings("unchecked")
    public static HttpMate theHttpMateInstanceUsedForTesting() {
        final MapMateSerializerAndDeserializer mapMate = mapMateIntegration(MAP_MATE).build();
        final HttpMate httpMate = anHttpMate()
                .serving(TestUseCase.class).forRequestPath("/test").andRequestMethods(GET, POST, PUT, DELETE)
                .serving(EchoBodyUseCase.class).forRequestPath("/echo_body").andRequestMethods(GET, POST, PUT, DELETE)
                .get("/mapped_exception", MappedExceptionUseCase.class)
                .get("/unmapped_exception", UnmappedExceptionUseCase.class)
                .get("/wild/<parameter>/card", WildCardUseCase.class)
                .get("/parameterized", ParameterizedUseCase.class)
                .get("/queryparameters", QueryParametersUseCase.class)
                .get("/headers", HeaderUseCase.class)
                .get("/headers_response", HeadersInResponseUseCase.class)
                .get("/echo_contenttype", EchoContentTypeUseCase.class)
                .get("/set_contenttype_in_response", SetContentTypeInResponseUseCase.class)
                .serving(EchoMultipartUseCase.class).forRequestPath("/multipart_echo").andRequestMethods(GET, POST, PUT, DELETE)
                .serving(MapMateUseCase.class).forRequestPath("/mapmate/<value1>").andRequestMethods(GET, POST)
                .get("/echo_path_and_query_parameters/<wildcard>", EchoPathAndQueryParametersUseCase.class)
                .get("/twoparameters", TwoParametersUseCase.class)
                .get("/void", VoidUseCase.class)
                .put("/multipart_and_mapmate", MultipartAndMapmateUseCase.class)

                .configured(configuratorForType(UseCasesModule.class, useCasesModule -> {
                    useCasesModule.addRequestMapperForType(Parameter.class, (targetType, map) -> new Parameter());
                    useCasesModule.addRequestMapperForType(WildcardParameter.class, (targetType, map) -> new WildcardParameter((String) map.get("parameter")));
                    useCasesModule.addRequestMapperForType(QueryParametersParameter.class, (targetType, map) -> new QueryParametersParameter((Map<String, String>) (Object) map));
                    useCasesModule.addRequestMapperForType(HeadersParameter.class, (targetType, map) -> new HeadersParameter((Map<String, String>) (Object) map));
                    useCasesModule.addRequestMapperForType(EchoPathAndQueryParametersValue.class, (targetType, map) -> new EchoPathAndQueryParametersValue((Map<String, String>) (Object) map));
                    useCasesModule.addRequestMapperForType(Parameter1.class, (targetType, map) -> {
                        final Object param1 = map.get("param1");
                        return new Parameter1((String) param1);
                    });
                    useCasesModule.addRequestMapperForType(Parameter2.class, (targetType, map) -> {
                        final Object param2 = map.get("param2");
                        return new Parameter2((String) param2);
                    });

                    useCasesModule.addResponseSerializerForType(HeadersInResponseReturnValue.class, value -> of(value.key, value.value));
                    useCasesModule.addResponseSerializerForType(SetContentTypeInResponseValue.class, value -> of("contentType", value.value));
                    useCasesModule.addResponseSerializer(Objects::isNull, object -> null);
                    useCasesModule.addResponseSerializerForType(String.class, string -> of("response", string));
                    useCasesModule.addResponseSerializerForType(ToStringWrapper.class, wrapper -> of("response", wrapper.toString()));

                    useCasesModule.setSerializerAndDeserializer(mapMate);
                }))
                .configured(toEnrichTheIntermediateMapWithAllRequestData())
                .configured(mapMate)
                .configured(toMapExceptionsOfType(NoHandlerFoundException.class, (exception, response) -> {
                    response.setStatus(METHOD_NOT_ALLOWED);
                    response.setBody("No use case found.");
                }))
                .configured(toMapExceptionsOfType(MappedException.class, (exception, response) -> response.setStatus(201)))

                .configured(toCustomizeResponsesUsing(metaData -> {
                    metaData.set(RESPONSE_STATUS, OK);
                    metaData.get(RESPONSE_HEADERS).put(Http.Headers.CONTENT_TYPE, "application/json");
                }))

                .configured(toLogToStderr())

                .build();
        return httpMate;
    }
}
