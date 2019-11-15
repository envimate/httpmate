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
import org.apache.http.HttpHost;
import org.apache.http.impl.pool.BasicConnPool;
import org.apache.http.impl.pool.BasicPoolEntry;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.lang.Thread.currentThread;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class PooledConnectionFactory implements ConnectionFactory {
    private static final int MAX_CONNECTIONS = 10;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;
    private final BasicConnPool connectionPool;

    static ConnectionFactory pooledConnectionFactory() {
        final BasicConnPool connectionPool = new BasicConnPool();
        connectionPool.setMaxTotal(MAX_CONNECTIONS);
        connectionPool.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        return new PooledConnectionFactory(connectionPool);
    }

    @Override
    public ConnectionFromPool getConnectionTo(final Endpoint endpoint) {
        final HttpHost target = new HttpHost(endpoint.host(), endpoint.port(), endpoint.protocol().identifier());
        final Future<BasicPoolEntry> future = connectionPool.lease(target, null);
        try {
            final BasicPoolEntry entry = future.get();
            return ConnectionFromPool.connectionFromPool(connectionPool, entry);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
        throw new RuntimeException("This should never happen");
    }

    @Override
    public void close() throws Exception {
        connectionPool.shutdown();
    }
}
