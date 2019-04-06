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

package com.envimate.httpmate.tests.givenwhenthen;

import com.envimate.httpmate.tests.givenwhenthen.builders.BodyBuilder;
import com.envimate.httpmate.tests.givenwhenthen.builders.HeaderBuilder;
import com.envimate.httpmate.tests.givenwhenthen.builders.MethodBuilder;
import com.envimate.httpmate.tests.givenwhenthen.builders.PathBuilder;
import com.envimate.httpmate.tests.givenwhenthen.client.HttpClientResponse;
import com.envimate.httpmate.tests.givenwhenthen.builders.MultipartElement;
import com.envimate.httpmate.tests.givenwhenthen.client.HttpClientWrapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.tests.givenwhenthen.Then.then;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class When implements PathBuilder, MethodBuilder, BodyBuilder, HeaderBuilder {
    private final HttpClientWrapper clientWrapper;
    private final StringBuilder log;
    private String path;
    private String method;
    private final Map<String, String> headers = new HashMap<>();
    private Object body;

    static When theWhen(final HttpClientWrapper clientWrapper,
                        final StringBuilder log) {
        return new When(clientWrapper, log);
    }

    @Override
    public MethodBuilder aRequestToThePath(final String path) {
        this.path = path;
        return this;
    }

    @Override
    public BodyBuilder viaTheGETMethod() {
        method = "GET";
        return this;
    }

    @Override
    public BodyBuilder viaThePOSTMethod() {
        method = "POST";
        return this;
    }

    @Override
    public BodyBuilder viaThePUTMethod() {
        method = "PUT";
        return this;
    }

    @Override
    public BodyBuilder viaTheDELETEMethod() {
        method = "DELETE";
        return this;
    }

    @Override
    public BodyBuilder viaTheOPTIONSMethod() {
        method = "OPTIONS";
        return this;
    }

    @Override
    public HeaderBuilder withAnEmptyBody() {
        this.body = null;
        return this;
    }

    @Override
    public HeaderBuilder withTheBody(final String body) {
        this.body = body;
        return this;
    }

    @Override
    public HeaderBuilder withTheMultipartBody(final MultipartBuilder multipartBuilder) {
        body = multipartBuilder.elements;
        return this;
    }

    @Override
    public HeaderBuilder withTheHeader(final String key, final String value) {
        headers.put(key, value);
        return this;
    }

    @Override
    public HeaderBuilder withContentType(final String contentType) {
        return withTheHeader("Content-Type", contentType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Then isIssued() {
        final HttpClientResponse response;
        if(body == null) {
            response = clientWrapper.issueRequestWithoutBody(path, method, headers);
        } else if(body instanceof String) {
            response = clientWrapper.issueRequestWithStringBody(path, method, headers, (String) body);
        } else {
            response = clientWrapper.issueRequestWithMultipartBody(path, method, headers, (List<MultipartElement>) body);
        }
        return then(response, log);
    }
}
