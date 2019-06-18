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

package com.envimate.httpmate.convenience.resource;

import com.envimate.httpmate.convenience.handler.HttpHandler;
import com.envimate.httpmate.convenience.handler.HttpRequest;
import com.envimate.httpmate.convenience.handler.HttpResponse;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.Thread.currentThread;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceHandler implements HttpHandler {
    private final String resourcePath;

    public static HttpHandler theResource(final String resourcePath) {
        validateNotNullNorEmpty(resourcePath, "resourcePath");
        return new ResourceHandler(resourcePath);
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse response) {
        final ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        final InputStream resourceAsStream = contextClassLoader.getResourceAsStream(resourcePath);
        response.setBody(resourceAsStream);
    }
}
