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

package com.envimate.httpmate.client;

import com.envimate.httpmate.client.body.Body;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.envimate.httpmate.client.HeaderKey.headerKey;
import static com.envimate.httpmate.client.HeaderValue.headerValue;
import static com.envimate.httpmate.client.HttpClientRequestBuilder.httpClientRequestBuilderImplementation;
import static com.envimate.httpmate.http.Http.Headers.CONTENT_TYPE;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRequest<T> {
    private final RequestPath path;
    private final String method;
    private final Map<HeaderKey, HeaderValue> headers;
    private final InputStream body;
    private final Class<T> targetType;

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aGetRequestToThePath(final String path) {
        return aRequest("GET", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aPostRequestToThePath(final String path) {
        return aRequest("POST", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aPutRequestToThePath(final String path) {
        return aRequest("PUT", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aDeleteRequestToThePath(final String path) {
        return aRequest("DELETE", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> anOptionsRequestToThePath(final String path) {
        return aRequest("OPTIONS", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aRequest(final String method, final String path) {
        return httpClientRequestBuilderImplementation(method, path);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> HttpClientRequest<T> httpClientRequest(
            final RequestPath requestPath,
            final String method,
            final Map<HeaderKey, HeaderValue> headers,
            final Optional<Body> bodyOptional,
            final Class<T> targetType) {
        final Map<HeaderKey, HeaderValue> fixedHeaders = new HashMap<>(headers);
        final InputStream bodyStream;
        if (bodyOptional.isPresent()) {
            final Body body = bodyOptional.get();
            body.contentType().ifPresent(contentType ->
                    fixedHeaders.put(headerKey(CONTENT_TYPE), headerValue(contentType))
            );
            bodyStream = body.inputStream();
        } else {
            bodyStream = null;
        }

        return new HttpClientRequest<>(requestPath, method, fixedHeaders, bodyStream, targetType);
    }

    public RequestPath path() {
        return this.path;
    }

    public String method() {
        return this.method;
    }

    public Map<String, String> headers() {
        final Map<String, String> stringMap = new HashMap<>();
        headers.forEach((key, value) -> stringMap.put(key.value(), value.value()));
        return stringMap;
    }

    public Optional<InputStream> body() {
        return Optional.ofNullable(this.body);
    }

    Class<T> targetType() {
        return targetType;
    }
}
