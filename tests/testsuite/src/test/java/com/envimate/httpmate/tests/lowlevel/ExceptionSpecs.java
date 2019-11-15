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

package com.envimate.httpmate.tests.lowlevel;

import com.envimate.httpmate.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;

public final class ExceptionSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void unmappedExceptionHasStatusCode500AsDefault(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/exception", (request, response) -> {
                            throw new UnsupportedOperationException();
                        })
                        .build()
        )
                .when().aRequestToThePath("/exception").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mappedExceptionsHaveStatusCode500AsDefault(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/exception", (request, response) -> {
                            throw new UnsupportedOperationException();
                        })
                        .configured(toMapExceptionsOfType(UnsupportedOperationException.class, (exception, response) -> response.setBody("this exception is mapped")))
                        .build()
        )
                .when().aRequestToThePath("/exception").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("this exception is mapped")
                .theStatusCodeWas(500);
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void checkedExceptionsCanBeMapped(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/test", (request, response) -> {
                            throw (RuntimeException) new Exception();
                        })
                        .configured(toMapExceptionsOfType(Exception.class, (exception, response) -> response.setStatus(501)))
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(501);
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void subtypesOfMappedExceptionsGetMapped(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/test", (request, response) -> {
                            throw new UnsupportedOperationException();
                        })
                        .configured(toMapExceptionsOfType(Exception.class, (exception, response) -> response.setStatus(501)))
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(501);
    }
}
