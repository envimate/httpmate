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
import com.envimate.httpmate.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.http.headers.cookies.CookieBuilder.cookie;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;
import static java.time.Instant.ofEpochMilli;
import static java.util.concurrent.TimeUnit.HOURS;

public final class CookieSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) -> response.setCookie("asdf", "qwer"))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeSetWithExpirationDate(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withExpiration(ofEpochMilli(123456789))))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Expires=Fri, 02 Jan 1970 10:17:36 GMT");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeSetWithMaxAge(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withMaxAge(1, HOURS)))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Max-Age=3600");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeSetWithDomainScope(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").exposedToAllSubdomainsOf("example.org", "foo.com")))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Domain=example.org,foo.com");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeSetWithPathScope(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").exposedOnlyToSubpathsOf("/docs", "/img")))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Path=/docs,/img");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void secureCookieCanBeSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").thatIsOnlySentViaHttps()))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; Secure");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void httpOnlyCookieCanBeSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").thatIsNotAccessibleFromJavaScript()))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; HttpOnly");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeSetWithStrictSameSitePolicy(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withSameSitePolicy(SameSitePolicy.STRICT)))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; SameSite=Strict");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeSetWithLaxSameSitePolicy(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withSameSitePolicy(SameSitePolicy.LAX)))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; SameSite=Lax");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeSetWithNoneSameSitePolicy(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.setCookie(cookie("asdf", "qwer").withSameSitePolicy(SameSitePolicy.NONE)))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"qwer\"; SameSite=None");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeInvalidated(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) ->
                        response.invalidateCookie("asdf"))
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "asdf=\"\"; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieCanBeReceived(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) -> {
                    final String cookie = request.cookies().getCookie("myCookie");
                    response.setBody(cookie);
                })
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Cookie", "myCookie=qwer").isIssued()
                .theResponseBodyWas("qwer");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void cookieWrappedInDoubleQuotesCanBeReceived(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
                .get("/cookie", (request, response) -> {
                    final String cookie = request.cookies().getCookie("myCookie");
                    response.setBody(cookie);
                })
                .build())
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Cookie", "myCookie=\"qwer\"").isIssued()
                .theResponseBodyWas("qwer");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void multipleCookiesInSameHeaderCanBeReceived(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMate()
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
