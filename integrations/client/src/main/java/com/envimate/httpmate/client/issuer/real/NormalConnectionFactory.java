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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.pool.BasicConnFactory;

import java.io.IOException;

import static com.envimate.httpmate.client.issuer.real.NormalConnection.normalConnection;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NormalConnectionFactory implements ConnectionFactory {
    private final BasicConnFactory basicConnFactory;

    static ConnectionFactory normalConnectionFactory() {
        final BasicConnFactory basicConnFactory = new BasicConnFactory(SocketConfig.DEFAULT, ConnectionConfig.DEFAULT);
        return new NormalConnectionFactory(basicConnFactory);
    }

    @Override
    public Connection getConnectionTo(final Endpoint endpoint) {
        final HttpHost target = new HttpHost(endpoint.host(), endpoint.port(), endpoint.protocol().identifier());
        try {
            final HttpClientConnection connection = basicConnFactory.create(target);
            return normalConnection(connection);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }
}
