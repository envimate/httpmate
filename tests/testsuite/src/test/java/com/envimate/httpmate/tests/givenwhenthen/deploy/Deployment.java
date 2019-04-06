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

package com.envimate.httpmate.tests.givenwhenthen.deploy;

import com.envimate.httpmate.HttpMate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Deployment {
    private final HttpMate httpMate;
    private final String protocol;
    private final String hostname;
    private final int port;
    private final String basePath;

    public static Deployment httpsDeploymentWithBasePath(final String hostname,
                                                         final int port,
                                                         final String basePath) {
        if(!basePath.startsWith("/")) {
            throw new IllegalArgumentException("basePath has to start with a '/'");
        }
        return new Deployment(null, "https", hostname, port, basePath);
    }

    public static Deployment httpDeployment(final String hostname, final int port) {
        return new Deployment(null, "http", hostname, port, "/");
    }

    public static Deployment bypassedDeployment(final HttpMate httpMate) {
        return new Deployment(httpMate, null, null, -1, null);
    }

    public HttpMate httpMate() {
        return httpMate;
    }

    public String protocol() {
        return protocol;
    }

    public int port() {
        return port;
    }

    public String hostname() {
        return hostname;
    }

    public String basePath() {
        return basePath;
    }

    public String baseUrl() {
        return protocol + "://" + hostname + ":" + port + basePath;
    }
}
