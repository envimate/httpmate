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

import com.envimate.httpmate.marshalling.MarshallingModule;
import com.envimate.httpmate.marshalling.UnsupportedContentTypeException;
import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Map;

import static com.envimate.httpmate.HttpMate.aLowLevelHttpMate;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_BODY_STRING;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_STATUS;
import static com.envimate.httpmate.chains.Configurator.configuratorForType;
import static com.envimate.httpmate.convenience.configurators.exceptions.ExceptionMappingConfigurator.toMapExceptions;
import static com.envimate.httpmate.http.headers.ContentType.fromString;
import static com.envimate.httpmate.marshalling.MarshallingModule.toMarshallBodiesBy;
import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;

@RunWith(Parameterized.class)
public final class MarshallingSpecs {

    public MarshallingSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Test
    public void unmarshallerCanBeSet() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(map -> {
                    final Object value = map.get("a");
                    response.setBody((String) value);
                }))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("qwer")).with(body -> Map.of("a", "b"))
                        .usingTheDefaultContentType(fromString("qwer")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("qwer").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("b");
    }

    @Test
    public void marshallerCanBeSet() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(response::setBody))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("qwer")).with(body -> Map.of("a", "b"))
                        .marshallingContentTypeInResponses(fromString("qwer")).with(map -> (String) map.get("a"))
                        .usingTheDefaultContentType(fromString("qwer")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("qwer").withTheHeader("Accept", "qwer").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("b");
    }

    @Test
    public void requestUsesContentTypeHeaderForUnmarshalling() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(map -> {
                    final Object value = map.get("a");
                    response.setBody((String) value);
                }))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("wrong")).with(body -> Map.of("a", "wrong"))
                        .unmarshallingContentTypeInRequests(fromString("right")).with(body -> Map.of("a", "right"))
                        .usingTheDefaultContentType(fromString("wrong")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("right").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("right");
    }

    @Test
    public void defaultContentTypeIsUsedForUnmarshallingIfNoContentTypeIsSpecified() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(map -> {
                    final Object value = map.get("a");
                    response.setBody((String) value);
                }))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("wrong")).with(body -> Map.of("a", "wrong"))
                        .unmarshallingContentTypeInRequests(fromString("right")).with(body -> Map.of("a", "right"))
                        .usingTheDefaultContentType(fromString("right")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("right");
    }

    @Test
    public void responseUsesContentTypeOfAcceptHeader() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(response::setBody))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("qwer")).with(body -> Map.of("a", "b"))
                        .marshallingContentTypeInResponses(fromString("wrong")).with(map -> "the wrong marshaller")
                        .marshallingContentTypeInResponses(fromString("right")).with(map -> "the right marshaller")
                        .usingTheDefaultContentType(fromString("qwer")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("qwer").withTheHeader("Accept", "right")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("right")
                .theResponseBodyWas("the right marshaller");
    }

    @Test
    public void responseIsMarshalledUsingContentTypeIfNoAcceptHeaderIsSet() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(response::setBody))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("right")).with(body -> Map.of("a", "b"))
                        .marshallingContentTypeInResponses(fromString("wrong")).with(map -> "the wrong marshaller")
                        .marshallingContentTypeInResponses(fromString("right")).with(map -> "the right marshaller")
                        .usingTheDefaultContentType(fromString("right")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("right")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("right")
                .theResponseBodyWas("the right marshaller");
    }

    @Test
    public void wildcardsInAcceptHeaderCanBeUsedToSpecifyResponseContentType() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(response::setBody))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("wrong/x")).with(body -> Map.of("a", "b"))
                        .marshallingContentTypeInResponses(fromString("wrong/x")).with(map -> "the wrong marshaller")
                        .marshallingContentTypeInResponses(fromString("right/x")).with(map -> "the right marshaller")
                        .usingTheDefaultContentType(fromString("wrong/x")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withTheHeader("Accept", "right/*")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("right/x")
                .theResponseBodyWas("the right marshaller");
    }

    @Test
    public void responseIsMarshalledUsingContentTypeIfAcceptHeaderAllowsMultipleMarshallers() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(response::setBody))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("right/x")).with(body -> Map.of("a", "b"))
                        .unmarshallingContentTypeInRequests(fromString("right/y")).with(body -> Map.of("a", "c"))
                        .marshallingContentTypeInResponses(fromString("right/x")).with(map -> "the right marshaller")
                        .marshallingContentTypeInResponses(fromString("right/y")).with(map -> "the wrong marshaller")
                        .usingTheDefaultContentType(fromString("right/y")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("right/x").withTheHeader("Accept", "right/*")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("right/x")
                .theResponseBodyWas("the right marshaller");
    }

    @Test
    public void responseIsMarshalledUsingDefaultContentTypeIfAcceptAndContentTypeHeaderCannotBeUsed() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(response::setBody))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("qwer")).with(body -> Map.of("a", "b"))
                        .unmarshallingContentTypeInRequests(fromString("asdf")).with(body -> Map.of("a", "c"))
                        .marshallingContentTypeInResponses(fromString("qwer")).with(map -> "right")
                        .usingTheDefaultContentType(fromString("qwer")))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("asdf").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("right");
    }

    @Test
    public void unknownUnmarshallerCanThrowException() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(response::setBody))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("qwer")).with(body -> Map.of("a", "b"))
                        .usingTheDefaultContentType(fromString("qwer")))
                .configured(configuratorForType(MarshallingModule.class,
                        marshallingModule -> marshallingModule.setThrowExceptionIfNoMarshallerFound(true)))
                .configured(toMapExceptions()
                        .ofType(UnsupportedContentTypeException.class)
                        .toResponsesUsing((exception, metaData) -> {
                            metaData.set(RESPONSE_STATUS, 501);
                            metaData.set(RESPONSE_BODY_STRING, exception.getMessage());
                        })
                        .ofAllRemainingTypesUsing((exception, metaData) -> metaData.set(RESPONSE_STATUS, 500)))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("asdf").isIssued()
                .theStatusCodeWas(501)
                .theResponseBodyWas("Content type 'asdf' is not supported; supported content types are: 'qwer'");
    }

    @Test
    public void unknownMarshallerCanThrowException() {
        given(aLowLevelHttpMate()
                .post("/", (request, response) -> request.bodyAsMap().ifPresent(response::setBody))
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("qwer")).with(body -> Map.of("a", "b"))
                        .usingTheDefaultContentType(fromString("qwer")))
                .configured(configuratorForType(MarshallingModule.class,
                        marshallingModule -> marshallingModule.setThrowExceptionIfNoMarshallerFound(true)))
                .configured(toMapExceptions()
                        .ofType(UnsupportedContentTypeException.class)
                        .toResponsesUsing((exception, metaData) -> {
                            metaData.set(RESPONSE_STATUS, 501);
                            metaData.set(RESPONSE_BODY_STRING, exception.getMessage());
                        })
                        .ofAllRemainingTypesUsing((exception, metaData) -> metaData.set(RESPONSE_STATUS, 500)))
                .build())
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("qwer").isIssued()
                .theStatusCodeWas(501)
                .theResponseBodyWas("Content type 'qwer' is not supported; supported content types are: ''");
    }
}
