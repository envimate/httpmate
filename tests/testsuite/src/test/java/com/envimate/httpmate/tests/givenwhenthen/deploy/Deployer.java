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

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.tests.givenwhenthen.client.ClientFactory;

import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.envimate.httpmate.jetty.JettyEndpoint.jettyEndpointFor;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.Deployment.httpDeployment;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.FreePortPool.freePort;

public interface Deployer {
    default Deployment retryUntilFreePortFound(final Function<Integer, Deployment> deploymentFactory) {
        cleanUp();
        for (int i = 0; i < 1000; i++) {
            final int port = freePort();
            try {
                return deploymentFactory.apply(port);
            } catch (RuntimeException e) {
                final Throwable cause = e.getCause();
                final boolean isPortInUseException = cause instanceof IOException && cause.getMessage().contains("Failed to bind to");
                if (!isPortInUseException) {
                    throw e;
                }
            }
        }
        throw new UnsupportedOperationException("Could not find a free port to run jetty on");
    }

    Deployment deploy(HttpMate httpMate);

    void cleanUp();

    List<ClientFactory> supportedClients();
}
