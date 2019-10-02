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

package com.envimate.httpmate.tests.lowlevel.usecase;

import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import com.envimate.httpmate.tests.lowlevel.usecase.usecases.FailInInitializerUseCase;
import com.envimate.httpmate.tests.lowlevel.usecase.usecases.MyUseCaseInitializationException;
import com.envimate.httpmate.usecases.usecase.SerializerAndDeserializer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Map;

import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_BODY_STRING;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_STATUS;
import static com.envimate.httpmate.convenience.configurators.exceptions.ExceptionMappingConfigurator.toMapExceptions;
import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;
import static com.envimate.httpmate.usecases.UseCaseDrivenBuilder.useCaseDrivenBuilder;

@RunWith(Parameterized.class)
public final class UseCaseSpecs {

    public UseCaseSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Ignore
    @Test
    public void exceptionInInitializerCanBeCaught() {
        given(useCaseDrivenBuilder()
                .get("/", FailInInitializerUseCase.class)
                .mappingRequestsAndResponsesUsing(new SerializerAndDeserializer() {
                    @Override
                    public <T> T deserialize(final Class<T> type, final Map<String, Object> map) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Map<String, Object> serialize(final Object event) {
                        throw new UnsupportedOperationException();
                    }
                })
                .configured(toMapExceptions()
                        .ofType(MyUseCaseInitializationException.class).toResponsesUsing((exception, metaData) -> {
                            metaData.set(RESPONSE_BODY_STRING, "The correct exception has been thrown");
                            metaData.set(RESPONSE_STATUS, 505);
                        }).ofAllRemainingTypesUsing((exception, metaData) -> {
                            metaData.set(RESPONSE_BODY_STRING, "The incorrect exception has been thrown");
                            metaData.set(RESPONSE_STATUS, 501);
                        }))
                .build())
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(505)
                .theResponseBodyWas("The correct exception has been thrown");
    }
}
