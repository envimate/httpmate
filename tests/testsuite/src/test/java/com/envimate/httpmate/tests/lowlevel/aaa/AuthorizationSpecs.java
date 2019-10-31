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

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.http.Http.StatusCodes.UNAUTHORIZED;
import static com.envimate.httpmate.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;

@RunWith(Parameterized.class)
public final class AuthorizationSpecs {

    public AuthorizationSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Test
    public void unauthorizedRequestsCanBeRejected() {
        given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the secret"))
                        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> false))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theResponseBodyWas("");
    }

    @Test
    public void rejectionCanBeConfigured() {
        given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the secret"))
                        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> false)
                                .rejectingUnauthorizedRequestsUsing((request, response) -> {
                                    response.setStatus(UNAUTHORIZED);
                                    response.setBody("You have been rejected");
                                }))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(401)
                .theResponseBodyWas("You have been rejected");
    }

    @Test
    public void routesCanBeExcludedFromAuthorization() {
        given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the secret"))
                        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> false).exceptRequestsTo("/secret"))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("the secret");
    }

    @Test
    public void authorizationCanBeLimitedToCertainRoutes() {
        given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the secret"))
                        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> false).onlyRequestsTo("/somethingElse"))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("the secret");
    }
}
