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
import com.envimate.httpmate.client.body.multipart.Part;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static com.envimate.httpmate.client.HeaderKey.headerKey;
import static com.envimate.httpmate.client.HeaderValue.headerValue;
import static com.envimate.httpmate.client.HttpClientRequest.httpClientRequest;
import static com.envimate.httpmate.client.QueryParameter.queryParameter;
import static com.envimate.httpmate.client.body.Body.bodyWithoutContentType;
import static com.envimate.httpmate.client.body.multipart.MultipartBodyCreator.createMultipartBody;
import static com.envimate.httpmate.util.Streams.stringToInputStream;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRequestBuilder<T> {
    private final String method;
    private final RequestPath path;
    private Body body;
    private final Map<HeaderKey, HeaderValue> headers = new HashMap<>();
    private Class<T> targetType;

    static HttpClientRequestBuilder<SimpleHttpResponseObject> httpClientRequestBuilderImplementation(
            final String method, final String path) {
        validateNotNullNorEmpty(method, "method");
        validateNotNull(path, "path");
        final RequestPath requestPath = RequestPath.parse(path);
        final HttpClientRequestBuilder<?> httpClientRequestBuilder = new HttpClientRequestBuilder<>(method, requestPath);
        return httpClientRequestBuilder.mappedTo(SimpleHttpResponseObject.class);
    }

    public HttpClientRequestBuilder<T> withAMultipartBodyWithTheParts(final Part... parts) {
        return withTheBody(createMultipartBody(parts));
    }

    public HttpClientRequestBuilder<T> withTheBody(final String body) {
        return withTheBody(stringToInputStream(body));
    }

    public HttpClientRequestBuilder<T> withTheBody(final InputStream body) {
        return withTheBody(bodyWithoutContentType(() -> body));
    }

    public HttpClientRequestBuilder<T> withTheBody(final Body body) {
        this.body = body;
        return this;
    }

    public HttpClientRequestBuilder<T> withContentType(final String contentType) {
        return withHeader("Content-type", contentType);
    }

    public HttpClientRequestBuilder<T> withHeader(final String key, final String value) {
        final HeaderKey headerKey = headerKey(key);
        final HeaderValue headerValue = headerValue(value);
        this.headers.put(headerKey, headerValue);
        return this;
    }

    public HttpClientRequestBuilder<T> withQueryParameter(final String key) {
        this.path.add(queryParameter(key));
        return this;
    }

    public HttpClientRequestBuilder<T> withQueryParameter(final String key, final String value) {
        this.path.add(queryParameter(key, value));
        return this;
    }

    public HttpClientRequestBuilder<String> mappedToString() {
        return mappedTo(String.class);
    }

    @SuppressWarnings("unchecked")
    public <X> HttpClientRequestBuilder<X> mappedTo(final Class<X> targetType) {
        this.targetType = (Class<T>) targetType;
        return (HttpClientRequestBuilder<X>) this;
    }

    HttpClientRequest<T> build(final BasePath basePath) {
        //if (isNull(rawPath)) {
        //final String query = createQuery(explicitQueryParameters);
        //}
        return httpClientRequest(path, method, headers, Optional.ofNullable(body), targetType);
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
