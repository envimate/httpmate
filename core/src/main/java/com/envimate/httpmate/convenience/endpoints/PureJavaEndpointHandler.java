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

package com.envimate.httpmate.convenience.endpoints;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.chains.MetaData;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.util.Streams.streamInputStreamToOutputStream;
import static com.envimate.httpmate.util.Streams.stringToInputStream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class PureJavaEndpointHandler implements HttpHandler {
    private final HttpMate httpMate;

    static HttpHandler javaOnlyEndpointHandler(final HttpMate httpMate) {
        return new PureJavaEndpointHandler(httpMate);
    }

    @Override
    public void handle(final HttpExchange httpExchange) {
        final String requestMethod = httpExchange.getRequestMethod();
        final String path = httpExchange.getRequestURI().getPath();
        final String query = httpExchange.getRequestURI().getQuery();
        final Map<String, String> queryParameters = queryToMap(query);
        final Map<String, String> headers = httpExchange.getRequestHeaders().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));
        final InputStream body = httpExchange.getRequestBody();

        final MetaData metaData = emptyMetaData();
        metaData.set(RAW_HEADERS, headers);
        metaData.set(RAW_QUERY_PARAMETERS, queryParameters);
        metaData.set(RAW_METHOD, requestMethod);
        metaData.set(PATH, path);
        metaData.set(BODY_STREAM, body);
        metaData.set(IS_HTTP_REQUEST, true);

        httpMate.handleRequest(metaData, httpResponse -> {
            final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
            responseHeaders.forEach((key, value) -> httpExchange.getResponseHeaders().put(key, singletonList(value)));
            final int responseStatus = metaData.get(RESPONSE_STATUS);
            httpExchange.sendResponseHeaders(responseStatus, 0);
            final OutputStream outputStream = httpExchange.getResponseBody();
            final InputStream responseBody = metaData.getOptional(STREAM_RESPONSE).orElseGet(() -> stringToInputStream(""));
            streamInputStreamToOutputStream(responseBody, outputStream);
        });
    }

    private static Map<String, String> queryToMap(final String query) {
        final Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (final String param : query.split("&")) {
            final String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
