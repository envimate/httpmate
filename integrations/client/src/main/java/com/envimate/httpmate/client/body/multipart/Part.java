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

package com.envimate.httpmate.client.body.multipart;

import com.envimate.httpmate.client.body.multipart.builder.FileNameStage;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;
import java.util.Optional;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Part {
    private final String controlName;
    private final String fileName;
    private final InputStream fileContent;

    public static FileNameStage aPartWithTheControlName(final String controlName) {
        return fileName -> content -> {
            validateNotNullNorEmpty(controlName, "controlName");
            validateNotNull(content, "content");
            return new Part(controlName, fileName, content);
        };
    }

    InputStream fileContent() {
        return fileContent;
    }

    String controlName() {
        return controlName;
    }

    Optional<String> fileName() {
        return ofNullable(fileName);
    }
}
