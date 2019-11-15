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

import static com.envimate.httpmate.tests.HttpMateTestConfigurations.theHttpMateInstanceUsedForTesting;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;

public final class HttpMateSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testGetRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"response\":\"foo\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testPostRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/test").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"response\":\"foo\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testPutRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/test").viaThePutMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"response\":\"foo\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testDeleteRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/test").viaTheDeleteMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"response\":\"foo\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testUseCaseWithParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/parameterized").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"response\":\"parameter\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testHeadersInRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/headers").viaTheGetMethod().withAnEmptyBody().withTheHeader("testheader", "foo").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"response\":\"foo\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testWildcardRoute(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/wild/foo/card").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"response\":\"foo\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testWildcardRouteWithEmptyMiddleWildcard(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/wild/card").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(405)
                .theResponseBodyWas("No use case found.");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testQueryParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/queryparameters?param1=derp&param2=").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyContains("param1\\u003dderp")
                .theResponseBodyContains("param2\\u003d");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testQueryParametersAndPathParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo_path_and_query_parameters/foo?test=bar").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyContains("test\\u003dbar")
                .theResponseBodyContains("wildcard\\u003dfoo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testUseCaseNotFoundExceptionHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/this_has_no_usecase").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(405)
                .theResponseBodyWas("No use case found.");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMapMate(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/mapmate/derp").viaThePostMethod().withTheBody("{value1=derp,value2=merp,value3=herp,value4=qerp}").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theJsonResponseEquals("" +
                        "{" +
                        "   value1: derp," +
                        "   value2: merp," +
                        "   value3: herp," +
                        "   value4: qerp" +
                        "}"
                );
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMapMateWithInjection(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/mapmate/derp?value2=merp").viaThePostMethod().withTheBody("{value4=qerp}").withTheHeader("value3", "herp").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theJsonResponseEquals("" +
                        "{" +
                        "   value1: derp," +
                        "   value2: merp," +
                        "   value3: herp," +
                        "   value4: qerp" +
                        "}"
                );
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMapMateOnlyWithInjectionAndWithoutBody(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/mapmate/derp").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("value2", "merp").withTheHeader("value3", "herp").withTheHeader("value4", "qerp").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theJsonResponseEquals("" +
                        "{" +
                        "   value1: derp," +
                        "   value2: merp," +
                        "   value3: herp," +
                        "   value4: qerp" +
                        "}"
                );
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testTwoUseCaseParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/twoparameters").viaTheGetMethod().withAnEmptyBody().withTheHeader("param1", "Hello").withTheHeader("param2", "World").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"response\":\"Hello World\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testVoidUseCase(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/void").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testContentTypeCanContainParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/void").viaTheGetMethod().withAnEmptyBody().withContentType("application/json; charset=iso-8859-1").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json");
    }
}
