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

package com.envimate.httpmate.tests.lowlevel.aaa;

import com.envimate.httpmate.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.security.SecurityConfigurators.*;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;
import static java.util.Optional.of;

public final class AuthenticationSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithHeader(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/username", (request, response) -> {
                            final String username = request.authenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingHeader("username", Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("username", "asdf").isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithCookie(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/username", (request, response) -> {
                            final String username = request.authenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingCookie("username", Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("Cookie", "username=asdf").isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithQueryParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/username", (request, response) -> {
                            final String username = request.authenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingQueryParameter("username", Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username?username=asdf").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithPathParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/username/<username>", (request, response) -> {
                            final String username = request.authenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingPathParameter("username", Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username/asdf").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithBody(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .post("/username", (request, response) -> {
                            final String username = request.authenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateRequestsUsing(request -> of(request.bodyString())).afterBodyProcessing())
                        .build()
        )
                .when().aRequestToThePath("/username").viaThePostMethod().withTheBody("asdf").isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithOAuth2BearerToken(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/username", (request, response) -> {
                            final String username = request.authenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingOAuth2BearerToken(Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("Authorization", "Bearer asdf").isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void routesCanBeExcludedFromAuthentication(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/username", (request, response) -> {
                            final String username = request.authenticationInformationAs(String.class).orElse("guest");
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingHeader("username", Optional::of).exceptRequestsTo("/username", "/somethingElse"))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("username", "asdf").isIssued()
                .theResponseBodyWas("guest");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void authenticationCanBeLimitedToCertainRoutes(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/username", (request, response) -> {
                            final String username = request.authenticationInformationAs(String.class).orElse("guest");
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingHeader("username", Optional::of).onlyRequestsTo("/somethingElse"))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("username", "asdf").isIssued()
                .theResponseBodyWas("guest");
    }
}
