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

import static com.envimate.httpmate.Configurators.toCustomizeResponsesUsing;
import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;
import static com.envimate.httpmate.tests.lowlevel.LowLevelHttpMateConfiguration.theLowLevelHttpMateInstanceUsedForTesting;

public final class LowLevelSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testBodyOfAPostRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaThePostMethod().withTheBody("This is a post request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a post request.");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testBodyOfAPutRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaThePutMethod().withTheBody("This is a put request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a put request.");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testBodyOfADeleteRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaTheDeleteMethod().withTheBody("This is a delete request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a delete request.");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testContentTypeInRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo_contenttype").viaTheGetMethod().withAnEmptyBody().withContentType("foobar").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foobar");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testRequestContentTypeIsCaseInsensitive(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo_contenttype").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("CONTENT-TYPE", "foo").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testContentTypeInResponse(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/set_contenttype_in_response").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("foobar");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testHeadersInResponse(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/headers_response").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("foo", "bar");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testLoggerCanBeSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/log").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theLogOutputStartedWith("INFO: foobar");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testFileDownload(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/download").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/x-msdownload")
                .theResponseBodyWas("download-content")
                .theReponseContainsTheHeader("Content-Disposition", "attachment; filename=\"foo.txt\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testDebugModule(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/internals").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyContains("digraph");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testEmptyTemplate(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/test", (request, response) -> response.setBody("OK"))
                        .configured(toCustomizeResponsesUsing(metaData -> {
                        }))
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("OK");
    }
}
