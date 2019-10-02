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

package com.envimate.httpmate.marshalling;

import com.envimate.httpmate.chains.Configurator;
import com.envimate.httpmate.http.headers.ContentType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.chains.Configurator.toUseModules;
import static com.envimate.httpmate.marshalling.MarshallingModule.emptyMarshallingModule;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarshallingModuleBuilder {
    private final MarshallingModule marshallingModule = emptyMarshallingModule();

    static MarshallingModuleBuilder toMarshallBodiesBy() {
        return new MarshallingModuleBuilder();
    }

    public With<Unmarshaller> unmarshallingContentTypeInRequests(final ContentType contentType) {
        validateNotNull(contentType, "contentType");
        return unmarshaller -> {
            validateNotNull(unmarshaller, "unmarshaller");
            marshallingModule.addUnmarshaller(contentType, unmarshaller);
            return this;
        };
    }

    public With<Marshaller> marshallingContentTypeInResponses(final ContentType contentType) {
        validateNotNull(contentType, "contentType");
        return marshaller -> {
            validateNotNull(marshaller, "marshaller");
            marshallingModule.addMarshaller(contentType, marshaller);
            return this;
        };
    }

    public Configurator usingTheDefaultContentType(final ContentType contentType) {
        validateNotNull(contentType, "contentType");
        marshallingModule.setDefaultContentType(contentType);
        return toUseModules(marshallingModule);
    }
}
