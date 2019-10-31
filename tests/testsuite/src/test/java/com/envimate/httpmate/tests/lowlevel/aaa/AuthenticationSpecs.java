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

import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Optional;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.security.SecurityConfigurators.*;
import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;
import static java.util.Optional.of;

@RunWith(Parameterized.class)
public final class AuthenticationSpecs {

    public AuthenticationSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Test
    public void requestsCanBeAuthenticatedWithHeader() {
        given(
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

    @Test
    public void requestsCanBeAuthenticatedWithCookie() {
        given(
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

    @Test
    public void requestsCanBeAuthenticatedWithQueryParameter() {
        given(
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

    @Test
    public void requestsCanBeAuthenticatedWithPathParameter() {
        given(
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

    @Test
    public void requestsCanBeAuthenticatedWithBody() {
        given(
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

    @Test
    public void requestsCanBeAuthenticatedWithOAuth2BearerToken() {
        given(
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

    @Test
    public void routesCanBeExcludedFromAuthentication() {
        given(
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

    @Test
    public void authenticationCanBeLimitedToCertainRoutes() {
        given(
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
