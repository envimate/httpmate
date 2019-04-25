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

import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;
import static com.envimate.httpmate.tests.lowlevel.LowLevelHttpMateConfiguration.theLowLevelHttpMateInstanceUsedForTesting;

@RunWith(Parameterized.class)
public final class LowLevelSpecs {

    public LowLevelSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Test
    public void testBodyOfAPostRequest() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaThePostMethod().withTheBody("This is a post request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a post request.");
    }

    @Test
    public void testBodyOfAPutRequest() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaThePutMethod().withTheBody("This is a put request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a put request.");
    }

    @Test
    public void testBodyOfADeleteRequest() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaTheDeleteMethod().withTheBody("This is a delete request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a delete request.");
    }

    @Test
    public void testContentTypeInRequest() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo_contenttype").viaTheGetMethod().withAnEmptyBody().withContentType("foobar").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foobar");
    }

    @Test
    public void testRequestContentTypeIsCaseInsensitive() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/echo_contenttype").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("CONTENT-TYPE", "foo").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foo");
    }

    @Test
    public void testContentTypeInResponse() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/set_contenttype_in_response").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("foobar");
    }

    @Test
    public void testHeadersInResponse() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/headers_response").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("foo", "bar");
    }

    @Test
    public void testLoggerCanBeSet() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/log").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theLogOutputStartedWith("foobar");
    }

    @Test
    public void testFileDownload() {
        given(theLowLevelHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/download").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/x-msdownload")
                .theResponseBodyWas("download-content")
                .theReponseContainsTheHeader("Content-Disposition", "foo.txt");
    }

    /*
        authentication
        authorization
        template
        empty template
        test exception mapping module
        test invalid route
        cors tests here
        test debug module
     */
}
