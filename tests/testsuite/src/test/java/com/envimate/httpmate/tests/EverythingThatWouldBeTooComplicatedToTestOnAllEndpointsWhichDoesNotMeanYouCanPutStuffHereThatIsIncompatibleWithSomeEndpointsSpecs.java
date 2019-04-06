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

import com.envimate.httpmate.request.HttpRequestMethod;
import com.envimate.httpmate.tests.usecases.unmappedexception.UnmappedExceptionUseCase;
import org.junit.Test;

import static com.envimate.httpmate.HttpMate.aHttpMateInstance;
import static com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient.deployerAndClient;
import static com.envimate.httpmate.tests.givenwhenthen.Given.givenTheHttpMateInstanceWithLogger;
import static com.envimate.httpmate.tests.givenwhenthen.client.shitty.ShittyClientFactory.theShittyTestClient;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.purejava.PureJavaDeployer.pureJavaDeployer;

public final class EverythingThatWouldBeTooComplicatedToTestOnAllEndpointsWhichDoesNotMeanYouCanPutStuffHereThatIsIncompatibleWithSomeEndpointsSpecs {

    public EverythingThatWouldBeTooComplicatedToTestOnAllEndpointsWhichDoesNotMeanYouCanPutStuffHereThatIsIncompatibleWithSomeEndpointsSpecs() {
        setCurrentDeployerAndClient(deployerAndClient(pureJavaDeployer(), theShittyTestClient()));
    }

    @Test
    public void testDefaultExceptionHandlerLogsTheException() {
        /*
        givenTheHttpMateInstanceWithLogger(logger -> aHttpMateInstance()
                .servingTheUseCase(UnmappedExceptionUseCase.class).forRequestPath("/test").andRequestMethod(HttpRequestMethod.GET)
                .mappingRequestsToUseCaseParametersByDefaultUsing((targetType, context) -> null)
                .serializingResponseObjectsByDefaultUsing((object, metaData) -> {
                })
                .configuredBy((httpMateBuilder, useCaseConfigurator) -> httpMateBuilder.configureLogger().usingLogger((message, metaData) -> logger.append(message))))
                .when().aRequestToThePath("/test").viaTheGETMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theLogOutputStartedWith("com.envimate.httpmate.tests.usecases.unmappedexception.UnmappedException\n" +
                        "\tat com.envimate.httpmate.tests.usecases.unmappedexception.UnmappedExceptionUseCase.unmappedException(UnmappedExceptionUseCase.java:30)");
        */
    }
}
