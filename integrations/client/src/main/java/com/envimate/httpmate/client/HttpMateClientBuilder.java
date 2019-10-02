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

package com.envimate.httpmate.client;

import com.envimate.httpmate.client.issuer.Issuer;
import com.envimate.httpmate.filtermap.FilterMapBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.envimate.httpmate.client.BasePath.basePath;
import static com.envimate.httpmate.client.HttpMateClient.httpMateClient;
import static com.envimate.httpmate.client.SimpleHttpResponseObject.httpClientResponse;
import static com.envimate.httpmate.client.UnsupportedTargetTypeException.unsupportedTargetTypeException;
import static com.envimate.httpmate.filtermap.FilterMapBuilder.filterMapBuilder;
import static com.envimate.httpmate.util.Streams.inputStreamToString;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMateClientBuilder {
    private final Function<BasePath, Issuer> issuer;
    private final FilterMapBuilder<Class<?>, ClientResponseMapper<?>> responseMappers;
    private BasePath basePath = basePath("");

    static HttpMateClientBuilder clientBuilder(final Function<BasePath, Issuer> issuer) {
        validateNotNull(issuer, "issuer");
        final HttpMateClientBuilder builder = new HttpMateClientBuilder(issuer, filterMapBuilder());
        builder.withResponseMapping(String.class, (response, targetType) -> {
            if (targetType.equals(String.class)) {
                return inputStreamToString(response.content());
            }
            throw unsupportedTargetTypeException(String.class, targetType);
        });
        builder.withResponseMapping(SimpleHttpResponseObject.class, (response, targetType) -> {
            if (targetType.equals(SimpleHttpResponseObject.class)) {
                final String body = inputStreamToString(response.content());
                return httpClientResponse(response.statusCode(), response.headers(), body);
            }
            throw unsupportedTargetTypeException(SimpleHttpResponseObject.class, targetType);
        });
        builder.withDefaultResponseMapping((response, targetType) -> {
            throw new RuntimeException(format(
                    "Cannot map response '%s' to type '%s' because no default response mapper was defined",
                    response.toString(), targetType.getName()));
        });
        return builder;
    }

    public HttpMateClientBuilder withBasePath(final String basePath) {
        this.basePath = basePath(basePath);
        return this;
    }

    public HttpMateClientBuilder withDefaultResponseMapping(final ClientResponseMapper<?> mapper) {
        validateNotNull(mapper, "mapper");
        responseMappers.setDefaultValue(mapper);
        return this;
    }

    public <T> HttpMateClientBuilder withResponseMapping(final Class<T> type,
                                                         final ClientResponseMapper<T> mapper) {
        validateNotNull(type, "type");
        validateNotNull(mapper, "mapper");
        return withResponseMapping(subtype(type), mapper);
    }

    @SuppressWarnings("unchecked")
    public <T> HttpMateClientBuilder withResponseMapping(final Predicate<Class<T>> filter,
                                                         final ClientResponseMapper<T> mapper) {
        validateNotNull(filter, "filter");
        validateNotNull(mapper, "mapper");
        responseMappers.put((Predicate<Class<?>>) (Object) filter, mapper);
        return this;
    }

    public HttpMateClient build() {
        final Issuer issuer = this.issuer.apply(basePath);
        return httpMateClient(issuer, basePath, responseMappers.build());
    }

    private static <T> Predicate<Class<T>> subtype(final Class<T> type) {
        return type::isAssignableFrom;
    }
}
