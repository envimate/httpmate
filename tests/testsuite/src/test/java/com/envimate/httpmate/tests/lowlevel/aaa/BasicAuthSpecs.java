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
import static com.envimate.httpmate.security.SecurityConfigurators.toDoBasicAuthWith;
import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;

@RunWith(Parameterized.class)
public final class BasicAuthSpecs {

    public BasicAuthSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Test
    public void unauthenticatedRequestsAreRejected() {
        given(
                anHttpMate()
                        .get("/secret", (request, response) -> response.setBody("the_secret"))
                        .configured(toDoBasicAuthWith((username, password) -> "asdf".equals(password)))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(401)
                .theReponseContainsTheHeader("WWW-Authenticate", "Basic");
    }

    @Test
    public void requestWithWrongCredentialsGetsRejected() {
        given(
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

    @Test
    public void requestWithCorrectCredentialsDoNotGetRejected() {
        given(
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

    @Test
    public void messageCanBeSet() {
        given(
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
