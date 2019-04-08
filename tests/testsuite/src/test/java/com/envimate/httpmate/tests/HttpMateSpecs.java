/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

package com.envimate.httpmate.tests;

import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static com.envimate.httpmate.tests.givenwhenthen.Given.givenTheTestHttpMateInstance;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.*;

@RunWith(Parameterized.class)
public final class HttpMateSpecs {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    public HttpMateSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @AfterClass
    public static void cleanUp() {
        cleanUpAllDeployers();
    }

    @Test
    public void testGETRequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/test").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("foo");
    }

    @Test
    public void testPOSTRequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/test").viaThePOSTMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("foo");
    }

    @Test
    public void testPUTRequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/test").viaThePUTMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("foo");
    }

    @Test
    public void testDELETERequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/test").viaTheDELETEMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("foo");
    }

    @Test
    public void testCorsOptionsRequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/the/path/does/not/matter/for/options/requests")
                .viaTheOPTIONSMethod().withAnEmptyBody()
                .withTheHeader("Access-Control-Request-Headers", "X-Custom-Header, Upgrade-Insecure-Requests")
                .withTheHeader("Access-Control-Request-Method", "POST, GET, OPTIONS").isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("Access-Control-Allow-Headers", "X-Custom-Header, Upgrade-Insecure-Requests")
                .theReponseContainsTheHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
                .theResponseBodyWas("OK");
    }

    @Test
    public void testCorsHeadersArePresent() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/test").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("foo")
                .theReponseContainsTheHeader("Access-Control-Allow-Origin", "*")
                .theReponseContainsTheHeader("Access-Control-Request-Method", "GET, POST, PUT, DELETE")
                .theReponseContainsTheHeader("Access-Control-Allow-Headers", "X-Custom-Header, Upgrade-Insecure-Requests");
    }

    @Test
    public void testUseCaseWithParameters() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/parameterized").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("parameter");
    }

    @Test
    public void testHeadersInRequest() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/headers").viaTheGETMethod().withAnEmptyBody().withTheHeader("testheader", "foo").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("foo");
    }

    @Test
    public void testHeadersInResponse() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/headers_response").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theReponseContainsTheHeader("foo", "bar");
    }

    @Test
    public void testContentTypeInResponse() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/set_contenttype_in_response").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("foobar");
    }

    @Test
    public void testWildcardRoute() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/wild/foo/card").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("foo");
    }

    @Test
    public void testWildcardRouteWithEmptyMiddleWildcard() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/wild/card").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(405)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("No use case found.");
    }

    @Test
    public void testQueryParameters() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/queryparameters?param1=derp&param2=").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyContains("param1=derp")
                .theResponseBodyContains("param2=");
    }

    @Test
    public void testQueryParametersAndPathParameters() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/echo_path_and_query_parameters/foo?test=bar").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyContains("test=bar")
                .theResponseBodyContains("wildcard=foo");
    }

    @Test
    public void testUseCaseNotFoundExceptionHandler() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/this_has_no_usecase").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(405)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("No use case found.");
    }

    @Test
    public void testMappedExceptionHandler() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/mapped_exception").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(201)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("");
    }

    @Test
    public void testDefaultExceptionHandlerDoesNotDiscloseTheExceptionInTheResponse() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/unmapped_exception").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("");
    }

    @Test
    public void testAuthenticationByHeader() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/authentication_echo").viaTheGETMethod().withAnEmptyBody().withTheHeader("username", "bob1").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Authenticated as: bob1");
    }

    @Test
    public void testAuthenticationByBody() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/authentication_echo").viaThePOSTMethod().withTheBody("{ \"username\": \"bob2\" }").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Authenticated as: bob2");
    }

    @Test
    public void testAuthenticationByQueryParameter() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/authentication_echo?username=bob3").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Authenticated as: bob3");
    }

    @Test
    public void testNoAuthentication() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/authentication_echo").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Authenticated as: guest");
    }

    @Test
    public void testAuthorization() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/authorized").viaThePOSTMethod().withAnEmptyBody().withTheHeader("username", "admin").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Welcome to the admin section!");
    }

    @Test
    public void testUnauthorized() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/authorized").viaThePOSTMethod().withAnEmptyBody().withTheHeader("username", "mallory").isIssued()
                .theStatusCodeWas(403)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Go away.");
    }

    @Test
    public void testMapMate() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/mapmate/derp").viaThePOSTMethod().withTheBody("{value1=derp,value2=merp,value3=herp,value4=qerp}").withContentType("application/json").isIssued()
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

    @Test
    public void testMapMateWithInjection() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/mapmate/derp?value2=merp").viaThePOSTMethod().withTheBody("{value4=qerp}").withTheHeader("value3", "herp").withContentType("application/json").isIssued()
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

    @Test
    public void testMapMateOnlyWithInjectionAndWithoutBody() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/mapmate/derp").viaTheGETMethod().withAnEmptyBody()
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

    @Test
    public void testTwoUseCaseParameters() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/twoparameters").viaTheGETMethod().withAnEmptyBody().withTheHeader("param1", "Hello").withTheHeader("param2", "World").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("Hello World");
    }


    @Test
    public void testVoidUseCase() {
        givenTheTestHttpMateInstance()
                .when().aRequestToThePath("/void").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("");
    }
}
