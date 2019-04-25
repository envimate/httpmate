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

package com.envimate.httpmate.tests.givenwhenthen.deploy.bypassed;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.tests.givenwhenthen.client.ClientFactory;
import com.envimate.httpmate.tests.givenwhenthen.deploy.Deployer;
import com.envimate.httpmate.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.httpmate.tests.givenwhenthen.client.real.BypassingHttpMateClientFactory.theBypassingHttpMateClient;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.Deployment.bypassedDeployment;
import static java.util.Collections.singletonList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BypassedDeployer implements Deployer {

    public static Deployer bypassedDeployer() {
        return new BypassedDeployer();
    }

    @Override
    public Deployment deploy(final HttpMate httpMate) {
        return bypassedDeployment(httpMate);
    }

    @Override
    public void cleanUp() {
    }

    @Override
    public List<ClientFactory> supportedClients() {
        return singletonList(theBypassingHttpMateClient());
    }

    @Override
    public String toString() {
        return "bypassed";
    }
}
