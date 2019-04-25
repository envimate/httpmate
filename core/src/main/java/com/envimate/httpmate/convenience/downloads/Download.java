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

package com.envimate.httpmate.convenience.downloads;

import com.envimate.httpmate.http.Http;
import com.envimate.httpmate.convenience.handler.HttpResponse;
import com.envimate.httpmate.http.ContentType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

import static com.envimate.httpmate.http.Http.Headers.CONTENT_DISPOSITION;
import static com.envimate.httpmate.http.Http.StatusCodes.OK;
import static com.envimate.httpmate.convenience.downloads.FileName.fileName;
import static com.envimate.httpmate.http.ContentType.fromString;
import static com.envimate.httpmate.util.Streams.stringToInputStream;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Download {
    private static final String DEFAULT_FILENAME = "filename";
    private static final String DEFAULT_CONTENT_TYPE = "application/x-msdownload";

    private final InputStream content;
    private final FileName fileName;
    private final ContentType contentType;

    public static Download download(final String content) {
        return download(content, DEFAULT_FILENAME);
    }

    public static Download download(final String content,
                                    final String fileName) {
        return download(content, fileName, DEFAULT_CONTENT_TYPE);
    }

    public static Download download(final String content,
                                    final String fileName,
                                    final String contentType) {
        validateNotNull(content, "content");
        validateNotNull(fileName, "fileName");
        validateNotNull(contentType, "contentType");
        final InputStream stream = stringToInputStream(content);
        return download(stream, fileName, contentType);
    }

    public static Download download(final InputStream content) {
        return download(content, DEFAULT_FILENAME);
    }

    public static Download download(final InputStream content,
                                    final String fileName) {
        return download(content, fileName, DEFAULT_CONTENT_TYPE);
    }

    public static Download download(final InputStream content,
                                    final String fileName,
                                    final String contentType) {
        validateNotNull(content, "content");
        validateNotNull(fileName, "fileName");
        validateNotNull(contentType, "contentType");
        return new Download(content, fileName(fileName), fromString(contentType));
    }

    void mapToResponse(final HttpResponse httpResponse) {
        httpResponse.setBody(content);
        httpResponse.setStatus(OK);
        httpResponse.addHeader(Http.Headers.CONTENT_TYPE, contentType.internalValueForMapping());
        httpResponse.addHeader(CONTENT_DISPOSITION, fileName.value());
    }
}
