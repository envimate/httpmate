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

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.security.SecurityConfigurators.toDoBasicAuthWith;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;

public final class BasicAuthSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void unauthenticatedRequestsAreRejected(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the_secret"))
                        .configured(toDoBasicAuthWith((username, password) -> "asdf".equals(password)))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(401)
                .theReponseContainsTheHeader("WWW-Authenticate", "Basic");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestWithWrongCredentialsGetsRejected(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the_secret"))
                        .configured(toDoBasicAuthWith((username, password) -> "asdf".equals(password)))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Authorization", encodeAsAuthorizationHeader("joe", "wrong")).isIssued()
                .theStatusCodeWas(401)
                .theReponseContainsTheHeader("WWW-Authenticate", "Basic");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestWithCorrectCredentialsDoNotGetRejected(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the_secret"))
                        .configured(toDoBasicAuthWith((username, password) -> "asdf".equals(password)))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Authorization", encodeAsAuthorizationHeader("joe", "asdf")).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("the_secret");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void messageCanBeSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the_secret"))
                        .configured(toDoBasicAuthWith((username, password) -> "asdf".equals(password)).withMessage("my message"))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(401)
                .theReponseContainsTheHeader("WWW-Authenticate", "Basic realm=\"my message\"");
    }

    private static String encodeAsAuthorizationHeader(final String username, final String password) {
        final String unencoded = format("%s:%s", username, password);
        final String encoded = getEncoder().encodeToString(unencoded.getBytes(UTF_8));
        return format("Basic %s", encoded);
    }
}
