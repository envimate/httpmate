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
import com.envimate.httpmate.http.headers.ContentType;
import com.envimate.httpmate.http.headers.accept.Accept;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.HttpMateChains.POST_INVOKE;
import static com.envimate.httpmate.HttpMateChains.PROCESS_BODY_STRING;
import static com.envimate.httpmate.http.Http.Headers.CONTENT_TYPE;
import static com.envimate.httpmate.http.headers.ContentType.fromString;
import static com.envimate.httpmate.http.headers.accept.Accept.fromMetaData;
import static com.envimate.httpmate.marshalling.MarshallingException.marshallingException;
import static com.envimate.httpmate.marshalling.ResponseContentTypeCouldNotBeDeterminedException.responseContentTypeCouldNotBeDeterminedException;
import static com.envimate.httpmate.marshalling.UnsupportedContentTypeException.unsupportedContentTypeException;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Objects.isNull;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarshallingModule implements ChainModule {
    private volatile ContentType defaultContentType;
    private final Map<ContentType, Unmarshaller> unmarshallers;
    private final Map<ContentType, Marshaller> marshallers;
    private volatile boolean throwExceptionIfNoMarshallerFound;

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
        if (defaultContentType == null) {
            defaultContentType = contentType;
        }
    }

    public void addMarshaller(final ContentType contentType, final Marshaller marshaller) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshaller, "marshaller");
        marshallers.put(contentType, marshaller);
        if (defaultContentType == null) {
            defaultContentType = contentType;
        }
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
        extender.appendProcessor(PROCESS_BODY_STRING, this::processUnmarshalling);
        extender.prependProcessor(POST_INVOKE, this::processMarshalling);
    }

    private void processUnmarshalling(final MetaData metaData) {
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

    private void processMarshalling(final MetaData metaData) {
        try {
            metaData.getOptional(RESPONSE_BODY_MAP).ifPresent(map -> {
                final ContentType responseContentType = determineResponseContentType(metaData);
                final Marshaller marshaller = marshallerFor(responseContentType);
                metaData.set(RESPONSE_CONTENT_TYPE, responseContentType);
                final String stringBody = marshaller.marshall(map);
                metaData.set(RESPONSE_BODY_STRING, stringBody);
            });

        } catch (final MarshallingException e) {
            if(metaData.getOptional(EXCEPTION).isEmpty()) {
                failIfConfiguredToDoSo(() -> marshallingException(e));
            }
        }
    }

    private Marshaller marshallerFor(final ContentType responseContentType) {
        final Marshaller marshaller = marshallers.get(responseContentType);
        if (isNull(marshaller)) {
            throw unsupportedContentTypeException(responseContentType, marshallers.keySet());
        }
        return marshaller;
    }

    private ContentType determineResponseContentType(final MetaData metaData) {
        final Optional<ContentType> responseContentType = responseContentType(metaData);
        if (responseContentType.isPresent()) {
            return responseContentType.get();
        }
        final Accept accept = fromMetaData(metaData);
        final List<ContentType> candidates = marshallers.keySet().stream()
                .filter(accept::contentTypeIsAccepted)
                .collect(toList());
        if (candidates.isEmpty()) {
            throw responseContentTypeCouldNotBeDeterminedException(metaData);
        }
        return metaData.getOptional(REQUEST_CONTENT_TYPE)
                .filter(candidates::contains)
                .orElseGet(() -> candidates.get(0));
    }

    private static Optional<ContentType> responseContentType(final MetaData metaData) {
        return metaData.getOptional(RESPONSE_HEADERS).flatMap(headers -> {
            if (headers.containsKey(CONTENT_TYPE)) {
                final String contentType = headers.get(CONTENT_TYPE);
                return of(fromString(contentType));
            } else {
                return empty();
            }
        });
    }

    private void failIfConfiguredToDoSo(final Supplier<RuntimeException> exceptionSupplier) {
        if (throwExceptionIfNoMarshallerFound) {
            throw exceptionSupplier.get();
        }
    }
}
