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

package com.envimate.httpmate.multipart;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Optional;

import static com.envimate.httpmate.util.Streams.inputStreamToString;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartPart {
    private final String controlName;
    private final String fileName;
    private final InputStream content;

    static MultipartPart multipartFormControl(final String controlName, final InputStream content) {
        validateNotNull(controlName, "controlName");
        validateNotNull(content, "content");
        return new MultipartPart(controlName, null, content);
    }

    static MultipartPart multipartFile(final String controlName, final String fileName, final InputStream content) {
        validateNotNull(controlName, "controlName");
        validateNotNull(fileName, "fileName");
        validateNotNull(content, "content");
        return new MultipartPart(controlName, fileName, content);
    }

    public String getControlName() {
        return controlName;
    }

    public Optional<String> getFileName() {
        return ofNullable(fileName);
    }

    public InputStream getContent() {
        return content;
    }

    public String readContentToString() {
        return inputStreamToString(content);
    }
}
