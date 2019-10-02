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

import static com.envimate.httpmate.client.HeaderKey.headerKey;
import static com.envimate.httpmate.client.HeaderValue.headerValue;
import static com.envimate.httpmate.client.HttpClientRequest.httpClientRequest;
import static com.envimate.httpmate.client.QueryParameterKey.queryParameterKey;
import static com.envimate.httpmate.client.QueryParameterValue.queryParameterValue;
import static com.envimate.httpmate.client.body.Body.bodyWithoutContentType;
import static com.envimate.httpmate.client.body.multipart.MultipartBodyCreator.createMultipartBody;
import static com.envimate.httpmate.util.Streams.stringToInputStream;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRequestBuilder<T> {
    private final String method;
    private final String path;
    private Body body;
    private final Map<HeaderKey, HeaderValue> headers = new HashMap<>();
    private final Map<QueryParameterKey, QueryParameterValue> explicitQueryParameters = new HashMap<>();
    private Class<T> targetType;

    static HttpClientRequestBuilder<SimpleHttpResponseObject> httpClientRequestBuilderImplementation(
            final String method, final String path) {
        validateNotNullNorEmpty(method, "method");
        validateNotNullNorEmpty(path, "path");
        final HttpClientRequestBuilder<?> httpClientRequestBuilder = new HttpClientRequestBuilder<>(method, path);
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

    public HttpClientRequestBuilder<T> withQueryParameter(final String key, final String value) {
        this.explicitQueryParameters.put(queryParameterKey(key), queryParameterValue(value));
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

    HttpClientRequest<T> build() {
        return httpClientRequest(path, method, headers, explicitQueryParameters, Optional.ofNullable(body), targetType);
    }
}
