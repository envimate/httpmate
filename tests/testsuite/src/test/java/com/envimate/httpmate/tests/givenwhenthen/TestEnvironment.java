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

package com.envimate.httpmate.tests.givenwhenthen;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.tests.givenwhenthen.client.ClientFactory;
import com.envimate.httpmate.tests.givenwhenthen.deploy.Deployer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployersWithOnlyShittyClient;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestEnvironment {
    private static final String PACKAGE = "com.envimate.httpmate.tests.givenwhenthen.TestEnvironment#";
    public static final String ALL_ENVIRONMENTS = PACKAGE + "allEnvironments";
    public static final String ONLY_SHITTY_CLIENT = PACKAGE + "onlyShittyClient";

    private final Deployer deployer;
    private final ClientFactory clientFactory;

    public static TestEnvironment testEnvironment(final Deployer deployer,
                                                  final ClientFactory clientFactory) {
        validateNotNull(deployer, "deployer");
        validateNotNull(clientFactory, "clientFactory");
        return new TestEnvironment(deployer, clientFactory);
    }

    public static List<TestEnvironment> allEnvironments() {
        return activeDeployers()
                .stream()
                .map(deployerAndClient -> testEnvironment(deployerAndClient.deployer(), deployerAndClient.clientFactory()))
                .collect(toList());
    }

    public static List<TestEnvironment> onlyShittyClient() {
        return activeDeployersWithOnlyShittyClient()
                .stream()
                .map(deployerAndClient -> testEnvironment(deployerAndClient.deployer(), deployerAndClient.clientFactory()))
                .collect(toList());
    }

    public Given given(final HttpMate httpMate) {
        return Given.given(httpMate, deployer, clientFactory);
    }
}
