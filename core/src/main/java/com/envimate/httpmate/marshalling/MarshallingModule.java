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

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.http.Headers;
import com.envimate.httpmate.http.headers.ContentType;
import com.envimate.httpmate.http.headers.accept.Accept;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.HttpMateChains.POST_INVOKE;
import static com.envimate.httpmate.HttpMateChains.PROCESS_BODY_STRING;
import static com.envimate.httpmate.http.Http.Headers.CONTENT_TYPE;
import static com.envimate.httpmate.http.headers.ContentType.fromString;
import static com.envimate.httpmate.http.headers.accept.Accept.fromHeaders;
import static com.envimate.httpmate.marshalling.UnsupportedContentTypeException.unsupportedContentTypeException;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarshallingModule implements ChainModule {
    private ContentType defaultContentType;
    private final Map<ContentType, Unmarshaller> unmarshallers;
    private final Map<ContentType, Marshaller> marshallers;
    private boolean throwExceptionIfNoMarshallerFound;

    public static MarshallingModuleBuilder toMarshallBodiesBy() {
        return MarshallingModuleBuilder.toMarshallBodiesBy();
    }

    public static MarshallingModule emptyMarshallingModule() {
        return new MarshallingModule(new HashMap<>(), new HashMap<>());
    }

    public void addUnmarshaller(final ContentType contentType, final Unmarshaller unmarshaller) {
        validateNotNull(contentType, "contentType");
        validateNotNull(unmarshaller, "unmarshaller");
        unmarshallers.put(contentType, unmarshaller);
    }

    public void addMarshaller(final ContentType contentType, final Marshaller marshaller) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshaller, "marshaller");
        marshallers.put(contentType, marshaller);
    }

    public void setDefaultContentType(final ContentType defaultContentType) {
        validateNotNull(defaultContentType, "defaultContentType");
        this.defaultContentType = defaultContentType;
    }

    public void setThrowExceptionIfNoMarshallerFound(final boolean throwExceptionIfNoMarshallerFound) {
        this.throwExceptionIfNoMarshallerFound = throwExceptionIfNoMarshallerFound;
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.appendProcessor(PROCESS_BODY_STRING, metaData -> {
                    metaData.getOptional(REQUEST_BODY_STRING).ifPresent(body -> {
                        final ContentType contentType = metaData.get(REQUEST_CONTENT_TYPE);

                        final Unmarshaller unmarshaller;
                        if (contentType.isEmpty()) {
                            unmarshaller = unmarshallers.get(defaultContentType);
                        } else {
                            unmarshaller = unmarshallers.get(contentType);
                        }

                        if (isNull(unmarshaller)) {
                            failIfConfiguredToDoSo(() -> unsupportedContentTypeException(contentType, unmarshallers.keySet()));
                        } else {
                            final Map<String, Object> mapBody = ofNullable(unmarshaller.unmarshall(body))
                                    .orElseGet(HashMap::new);
                            metaData.set(REQUEST_BODY_MAP, mapBody);
                        }
                    });
                }
        );

        extender.prependProcessor(POST_INVOKE, metaData -> {
                    final ContentType responseContentType = determineResponseContentType(metaData);
                    metaData.set(RESPONSE_CONTENT_TYPE, responseContentType);

                    metaData.getOptional(RESPONSE_BODY_MAP).ifPresent(map -> {
                        final Marshaller marshaller = marshallers.get(responseContentType);
                        if (isNull(marshaller)) {
                            failIfConfiguredToDoSo(() ->
                                    unsupportedContentTypeException(responseContentType, marshallers.keySet()));
                        } else {
                            final String stringBody = marshaller.marshall(map);
                            metaData.set(RESPONSE_BODY_STRING, stringBody);
                        }
                    });
                }
        );
    }

    private ContentType determineResponseContentType(final MetaData metaData) {
        final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
        if(responseHeaders.containsKey(CONTENT_TYPE)) {
            final String contentType = responseHeaders.get(CONTENT_TYPE);
            return fromString(contentType);
        }
        final Headers headers = metaData.get(REQUEST_HEADERS);
        final Accept accept = fromHeaders(headers);
        final List<ContentType> candidates = marshallers.keySet().stream()
                .filter(accept::contentTypeIsAccepted)
                .collect(toList());
        if (candidates.isEmpty()) {
            return defaultContentType;
        }
        final ContentType requestContentType = metaData.get(REQUEST_CONTENT_TYPE);
        if (candidates.contains(requestContentType)) {
            return requestContentType;
        }
        return candidates.iterator().next();
    }

    private void failIfConfiguredToDoSo(final Supplier<RuntimeException> exceptionSupplier) {
        if (throwExceptionIfNoMarshallerFound) {
            throw exceptionSupplier.get();
        }
    }
}
