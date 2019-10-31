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

import com.envimate.httpmate.http.headers.cookies.SameSitePolicy;
import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.http.headers.cookies.CookieBuilder.cookie;
import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;
import static java.time.Instant.ofEpochMilli;
import static java.util.concurrent.TimeUnit.HOURS;

@RunWith(Parameterized.class)
public final class CookieSpecs {

    public CookieSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Test
    public void cookieCanBeSet() {
        given(anHttpMate()
                .get("/cookie", (request, response) -> response.setCookie("asdf", "qwer"))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"");
    }

    @Test
    public void cookieCanBeSetWithExpirationDate() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withExpiration(ofEpochMilli(123456789))))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Expires=Fri, 02 Jan 1970 10:17:36 GMT");
    }

    @Test
    public void cookieCanBeSetWithMaxAge() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withMaxAge(1, HOURS)))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Max-Age=3600");
    }

    @Test
    public void cookieCanBeSetWithDomainScope() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").exposedToAllSubdomainsOf("example.org", "foo.com")))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Domain=example.org,foo.com");
    }

    @Test
    public void cookieCanBeSetWithPathScope() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").exposedOnlyToSubpathsOf("/docs", "/img")))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Path=/docs,/img");
    }

    @Test
    public void secureCookieCanBeSet() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").thatIsOnlySentViaHttps()))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Secure");
    }

    @Test
    public void httpOnlyCookieCanBeSet() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").thatIsNotAccessibleFromJavaScript()))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; HttpOnly");
    }

    @Test
    public void cookieCanBeSetWithStrictSameSitePolicy() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withSameSitePolicy(SameSitePolicy.STRICT)))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; SameSite=Strict");
    }

    @Test
    public void cookieCanBeSetWithLaxSameSitePolicy() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withSameSitePolicy(SameSitePolicy.LAX)))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; SameSite=Lax");
    }

    @Test
    public void cookieCanBeSetWithNoneSameSitePolicy() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withSameSitePolicy(SameSitePolicy.NONE)))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; SameSite=None");
    }

    @Test
    public void cookieCanBeInvalidated() {
        given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.invalidateCookie("asdf"))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"\"; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

    @Test
    public void cookieCanBeReceived() {
        given(anHttpMate()
                .get("/cookie", (request, response) -> {
                    final String cookie = request.cookies().getCookie("myCookie");
                    response.setBody(cookie);
                })
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Cookie", "myCookie=qwer").isIssued()
                .theResponseBodyWas("qwer");
    }

    @Test
    public void cookieWrappedInDoubleQuotesCanBeReceived() {
        given(anHttpMate()
                .get("/cookie", (request, response) -> {
                    final String cookie = request.cookies().getCookie("myCookie");
                    response.setBody(cookie);
                })
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Cookie", "myCookie=\"qwer\"").isIssued()
                .theResponseBodyWas("qwer");
    }

    @Test
    public void multipleCookiesInSameHeaderCanBeReceived() {
        given(anHttpMate()
                .get("/cookie", (request, response) -> {
                    final String cookie1 = request.cookies().getCookie("cookie1");
                    final String cookie2 = request.cookies().getCookie("cookie2");
                    response.setBody(cookie1 + " and " + cookie2);
                })
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Cookie", "cookie1=qwer; cookie2=asdf").isIssued()
                .theResponseBodyWas("qwer and asdf");
    }
}
