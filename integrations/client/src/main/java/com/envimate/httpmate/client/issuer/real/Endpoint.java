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

package com.envimate.httpmate.client.issuer.real;

import com.envimate.httpmate.client.BasePath;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.StringJoiner;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class Endpoint {
    private final Protocol protocol;
    private final String host;
    private final int port;
    private final BasePath basePath;

    static Endpoint endpoint(final Protocol protocol,
                             final String host,
                             final int port,
                             final BasePath basePath) {
        validateNotNull(protocol, "protocol");
        validateNotNull(host, "host");
        validateNotNull(port, "port");
        validateNotNull(basePath, "basePath");
        return new Endpoint(protocol, host, port, basePath);
    }

    String host() {
        return this.host;
    }

    int port() {
        return this.port;
    }

    Protocol protocol() {
        return this.protocol;
    }

    String toUrl(final String path,
                 final Map<String, String> queryParameters) {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(this.protocol.identifier());
        urlBuilder.append("://");
        urlBuilder.append(this.host);
        urlBuilder.append(":");
        urlBuilder.append(this.port);
        final String fixedPath = basePath.concatenateWithStartingAndTrailingSlash(path);
        urlBuilder.append(fixedPath);
        final String queryParametersString = createQuery(queryParameters);
        urlBuilder.append(queryParametersString);
        return urlBuilder.toString();
    }

    private static String createQuery(final Map<String, String> queryParameters) {
        if (queryParameters.isEmpty()) {
            return "";
        }
        final StringJoiner joiner = new StringJoiner("&", "?", "");
        queryParameters.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }
}
