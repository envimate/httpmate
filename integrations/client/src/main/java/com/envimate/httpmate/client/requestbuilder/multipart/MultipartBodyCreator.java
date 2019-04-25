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

package com.envimate.httpmate.client.requestbuilder.multipart;

import com.envimate.httpmate.client.requestbuilder.Body;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;

import java.io.*;

import static com.envimate.httpmate.client.requestbuilder.multipart.MultipartRequestException.multipartRequestException;
import static java.lang.String.format;

public final class MultipartBodyCreator {
    private static final String BOUNDARY = "abcdefggggg";
    private static final String CONTENT_TYPE = format("multipart/form-data; boundary=%s", BOUNDARY);

    private MultipartBodyCreator() {
    }

    @SuppressWarnings("deprecation")
    public static Body createMultipartBody(final Part... parts) {
        return Body.bodyWithContentType(() -> {
            final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setBoundary(BOUNDARY);
            for (final Part part : parts) {
                final String controlName = part.controlName();
                final String fileName = part.fileName().orElse(null);
                final InputStream inputStream = part.fileContent();
                final InputStreamBody body = new InputStreamBody(inputStream, fileName);
                final FormBodyPart formBodyPart = new FormBodyPart(controlName, body);
                multipartEntityBuilder.addPart(formBodyPart);
            }
            final HttpEntity entity = multipartEntityBuilder.build();
            try {
                final PipedInputStream inputStream = new PipedInputStream();
                final PipedOutputStream pipedOutputStream = new PipedOutputStream(inputStream);
                new Thread(() -> {
                    try (OutputStream outputStream = pipedOutputStream) {
                        entity.writeTo(outputStream);
                    } catch (final IOException e) {
                        throw multipartRequestException(e);
                    }
                }).start();
                return inputStream;
            } catch (final IOException e) {
                throw multipartRequestException(e);
            }
        }, CONTENT_TYPE);
    }
}
