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

package com.envimate.httpmate.multipart.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.multipart.internal.SpecialServletInputStream.servletInputStreamBackedBy;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MockHttpServletRequest extends AbstractHttpServletRequestWithMockImplementations {
    private final InputStream body;
    private final String contentType;
    private final Map<String, String> headers;

    static HttpServletRequest mockHttpServletRequest(final InputStream body, final String contentType) {
        validateNotNull(body, "body");
        validateNotNull(contentType, "contentType");
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        return new MockHttpServletRequest(body, contentType, headers);
    }

    @Override
    public String getHeader(final String headerName) {
        return this.headers.get(headerName);
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletInputStream getInputStream() {
        return servletInputStreamBackedBy(body);
    }
}
