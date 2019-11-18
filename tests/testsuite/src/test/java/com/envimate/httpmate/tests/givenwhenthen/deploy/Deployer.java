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

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.envimate.httpmate.tests.givenwhenthen.deploy.FreePortPool.freePort;

public interface Deployer {
    default Deployment retryUntilFreePortFound(final Function<Integer, Deployment> deploymentFactory) {
        cleanUp();
        final List<Exception> exceptions = new LinkedList<>();
        for (int i = 0; i < 3; ++i) {
            final int port = freePort();
            try {
                return deploymentFactory.apply(port);
            } catch (final Exception e) {
                exceptions.add(e);
            }
        }
        final String message = "Failed three times to use supposedly free port.";
        System.err.println(message);
        exceptions.forEach(Throwable::printStackTrace);
        final Exception lastException = exceptions.get(exceptions.size() - 1);
        throw new IllegalStateException(message, lastException);
    }

    Deployment deploy(HttpMate httpMate);

    void cleanUp();

    List<ClientFactory> supportedClients();
}
