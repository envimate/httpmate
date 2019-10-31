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

package com.envimate.httpmate.handler.http;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.http.headers.ContentType;
import com.envimate.httpmate.http.headers.cookies.CookieBuilder;
import com.envimate.httpmate.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.http.Http.Headers.*;
import static com.envimate.httpmate.http.Http.StatusCodes.FOUND;
import static com.envimate.httpmate.http.headers.ContentType.fromString;
import static com.envimate.httpmate.http.headers.cookies.CookieBuilder.cookie;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.time.Instant.EPOCH;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpResponse {
    private final MetaData metaData;

    public static HttpResponse httpResponse(final MetaData metaData) {
        validateNotNull(metaData, "metaData");
        return new HttpResponse(metaData);
    }

    public void addHeader(final String key, final String value) {
        metaData.get(RESPONSE_HEADERS).put(key, value);
    }

    public void setCookie(final CookieBuilder cookie) {
        Validators.validateNotNull(cookie, "cookie");
        addHeader(SET_COOKIE, cookie.build());
    }

    public void setCookie(final String name, final String value) {
        setCookie(cookie(name, value));
    }

    public void invalidateCookie(final String name) {
        setCookie(cookie(name, "").withExpiration(EPOCH));
    }

    public void setContentType(final String contentType) {
        setContentType(fromString(contentType));
    }

    public void setContentType(final ContentType contentType) {
        validateNotNull(contentType, "contentType");
        addHeader(CONTENT_TYPE, contentType.valueWithComment());
    }

    public void asDownloadWithFilename(final String filename) {
        validateNotNull(filename, "filename");
        final String contentDispositionHeader = format("attachment; filename=\"%s\"", filename);
        addHeader(CONTENT_DISPOSITION, contentDispositionHeader);
        setContentType("application/x-msdownload");
    }

    public void redirectTo(final String target) {
        validateNotNull(target, "target");
        setStatus(FOUND);
        addHeader(LOCATION, target);
    }

    public void setStatus(final int status) {
        metaData.set(RESPONSE_STATUS, status);
    }

    public void setBody(final Map<String, Object> map) {
        metaData.set(RESPONSE_BODY_MAP, map);
    }

    public void setBody(final String body) {
        metaData.set(RESPONSE_BODY_STRING, body);
    }

    public void setBody(final InputStream inputStream) {
        metaData.set(RESPONSE_STREAM, inputStream);
    }

    public void setFileAsBody(final String path) {
        validateNotNullNorEmpty(path, "path");
        final File file = new File(path);
        setFileAsBody(file);
    }

    public void setFileAsBody(final File file) {
        validateNotNull(file, "file");
        try {
            final FileInputStream stream = new FileInputStream(file);
            setBody(stream);
        } catch (final IOException e) {
            throw new RuntimeException(format("Exception during loading of file '%s'", file.getAbsolutePath()), e);
        }
    }

    public void setJavaResourceAsBody(final String path) {
        validateNotNullNorEmpty(path, "path");
        final ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        final InputStream stream = contextClassLoader.getResourceAsStream(path);
        setBody(stream);
    }
}
