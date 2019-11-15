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

package com.envimate.httpmate.tests.lowlevel.mapmate;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.tests.givenwhenthen.TestEnvironment;
import com.envimate.httpmate.tests.lowlevel.mapmate.usecases.MyUseCase;
import com.envimate.httpmate.tests.lowlevel.mapmate.usecases.domain.MyRequest;
import com.envimate.mapmate.builder.MapMate;
import com.envimate.mapmate.deserialization.Unmarshaller;
import com.google.gson.Gson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.http.headers.ContentType.fromString;
import static com.envimate.httpmate.mapmate.MapMateConfigurator.toUseMapMate;
import static com.envimate.httpmate.mapmate.MapMateIntegration.toMarshalRequestAndResponseBodiesUsingMapMate;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;
import static com.envimate.mapmate.builder.MapMate.aMapMate;
import static com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;
import static com.envimate.mapmate.marshalling.MarshallingType.marshallingType;

public final class MapMateSpecs {

    private static HttpMate httpMate() {
        final MapMate mapMate = aMapMate()
                .usingMarshaller(marshallingType("custom"), o -> "custom_marshalled", new Unmarshaller() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T unmarshal(final String input, final Class<T> type) {
                        return (T) Map.of("key", "value");
                    }
                })
                .usingRecipe(urlEncodedMarshaller())
                .build();
        return anHttpMate()
                .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                .configured(toMarshalRequestAndResponseBodiesUsingMapMate(mapMate)
                        .matchingTheContentType(fromString("custom")).toTheMarshallerType(marshallingType("custom")))
                .build();
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMateIntegrationCanUnmarshalCustomFormat(final TestEnvironment testEnvironment) {
        testEnvironment.given(httpMate())
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("x").withContentType("custom").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("custom")
                .theResponseBodyWas("custom_marshalled");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMateIntegrationCanUnmarshalFormEncodedButDoesNotMarshalFormEncodedByDefault(final TestEnvironment testEnvironment) {
        testEnvironment.given(httpMate())
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("a=b").withContentType("application/x-www-form-urlencoded").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("custom")
                .theResponseBodyWas("custom_marshalled");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMateIntegrationCorrectlyUnmarshallsWithoutSpecifiedRequestContentType(final TestEnvironment testEnvironment) {
        final Gson gson = new Gson();
        final MapMate mapMate = aMapMate(MyRequest.class.getPackageName())
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        testEnvironment.given(
                anHttpMate()
                        .post("/", MyUseCase.class)
                        .configured(toUseMapMate(mapMate))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"foo\", \"field2\": \"bar\"}").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("{}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMateIntegrationCanHelpWithValidation(final TestEnvironment testEnvironment) {
        final Gson gson = new Gson();
        final MapMate mapMate = aMapMate(MyRequest.class.getPackageName())
                .withExceptionIndicatingValidationError(IllegalArgumentException.class)
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        testEnvironment.given(
                anHttpMate()
                        .post("/", MyUseCase.class)
                        .configured(toUseMapMate(mapMate))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"wrong\", \"field2\": \"wrong\"}").withContentType("application/json").isIssued()
                .theStatusCodeWas(500)
                .theJsonResponseEquals("" +
                        "{" +
                        "\"errors\":[" +
                        "{\"path\":\"field1\",\"message\":\"customPrimitive1 is wrong\"}," +
                        "{\"path\":\"field2\",\"message\":\"customPrimitive2 is wrong\"}" +
                        "]}");

    }
}
