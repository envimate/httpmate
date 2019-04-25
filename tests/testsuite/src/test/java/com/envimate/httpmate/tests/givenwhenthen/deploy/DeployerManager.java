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
import com.envimate.httpmate.tests.givenwhenthen.client.ClientFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient.deployerAndClient;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.jetty.JettyDeployer.jettyDeployer;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.purejava.PureJavaDeployer.pureJavaDeployer;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.servlet.ServletDeployer.servletDeployer;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.spark.SparkDeployer.sparkDeployer;
import static java.util.Arrays.asList;

public final class DeployerManager {
    //private static final Collection<Deployer> ACTIVE_DEPLOYERS =
    // asList(bypassedDeployer(), jettyDeployer(), sparkDeployer(), pureJavaDeployer(), servletDeployer(), awsDeployer());
    private static final Collection<Deployer> ACTIVE_DEPLOYERS =
            asList(jettyDeployer(), sparkDeployer(), pureJavaDeployer(), servletDeployer());
    private static DeployerAndClient currentDeployerAndClient;

    private DeployerManager() {
    }

    public static Collection<DeployerAndClient> activeDeployers() {
        final List<DeployerAndClient> deployerAndClients = new LinkedList<>();
        ACTIVE_DEPLOYERS.forEach(deployer -> deployer.supportedClients().forEach(clientFactory -> {
            final DeployerAndClient deployerAndClient = deployerAndClient(deployer, clientFactory);
            deployerAndClients.add(deployerAndClient);
        }));
        return deployerAndClients;
    }

    public static void setCurrentDeployerAndClient(final DeployerAndClient currentDeployerAndClient) {
        DeployerManager.currentDeployerAndClient = currentDeployerAndClient;
    }

    public static Deployer currentDeployer() {
        if (currentDeployerAndClient == null) {
            throw new RuntimeException("Deployer has not been set.");
        }
        return currentDeployerAndClient.deployer();
    }

    public static ClientFactory currentClientFactory() {
        if (currentDeployerAndClient == null) {
            throw new RuntimeException("Deployer has not been set.");
        }
        return currentDeployerAndClient.clientFactory();
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
