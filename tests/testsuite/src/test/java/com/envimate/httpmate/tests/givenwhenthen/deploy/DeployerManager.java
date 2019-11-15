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

package com.envimate.httpmate.tests.givenwhenthen.deploy;

import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import com.envimate.httpmate.tests.givenwhenthen.client.shitty.ShittyClientFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient.deployerAndClient;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.jetty.JettyDeployer.jettyDeployer;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.purejava.PureJavaDeployer.pureJavaDeployer;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.servlet.ServletDeployer.servletDeployer;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public final class DeployerManager {
    private static final Collection<Deployer> ACTIVE_DEPLOYERS = asList(jettyDeployer(), pureJavaDeployer(), servletDeployer());

    private DeployerManager() {
    }

    public static Collection<DeployerAndClient> activeDeployers() {
        return ACTIVE_DEPLOYERS.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .map(clientFactory -> deployerAndClient(deployer, clientFactory)))
                .collect(toList());
    }

    public static Collection<DeployerAndClient> activeDeployersWithOnlyShittyClient() {
        return ACTIVE_DEPLOYERS.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .filter(clientFactory -> clientFactory instanceof ShittyClientFactory)
                        .map(clientFactory -> deployerAndClient(deployer, clientFactory)))
                .collect(toList());
    }

    public static void cleanUpAllDeployers() {
        final List<Exception> exceptions = new LinkedList<>();
        ACTIVE_DEPLOYERS.forEach(deployer -> {
            try {
                deployer.cleanUp();
            } catch (final Exception e) {
                e.printStackTrace();
                exceptions.add(e);
            }
        });
        if (!exceptions.isEmpty()) {
            throw new RuntimeException("Exception during cleanup", exceptions.get(0));
        }
    }
}
