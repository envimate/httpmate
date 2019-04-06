/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

package com.envimate.httpmate.builder;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.response.HttpResponse;
import com.envimate.messageMate.useCaseAdapter.mapping.ResponseMapper;

import java.util.function.Predicate;

import static com.envimate.messageMate.useCaseAdapter.mapping.ResponseFilters.areOfType;
import static com.envimate.messageMate.useCaseAdapter.mapping.ResponseFilters.failWithMessage;

public interface SerializationStage<T> {

    /**
     * Enters a fluent builder that configures a {@link ResponseMapper} that will be used to serialize a use case return value
     * to a {@link HttpResponse} if the use case return value matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link ResponseMapper} should be used on the
     *               respective use case return value
     * @return the next step in the fluent builder
     */
    Using<SerializationStage<T>, ResponseMapper<Object>> serializingResponseObjectsThat(Predicate<Object> filter);

    /**
     * Configures the default {@link ResponseMapper} that will be used to serialize a use case return value
     * to a {@link HttpResponse} if no {@link ResponseMapper} configured under
     * {@link SerializationStage#serializingResponseObjectsThat(Predicate)},
     * {@link SerializationStage#serializingResponseObjectsOfType(Class)}, etc. matches the use case return value.
     *
     * @param mapper a {@link ResponseMapper}
     * @return the next step in the fluent builder
     */
    T serializingResponseObjectsByDefaultUsing(ResponseMapper<Object> mapper);

    /**
     * Enters a fluent builder that configures a {@link ResponseMapper} that will be used to serialize a use case return value
     * to a {@link HttpResponse} if the use case return value is of the specified type.
     *
     * @param type the type of use case return values that will be serialized by the {@link ResponseMapper}
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <X> Using<SerializationStage<T>, ResponseMapper<X>> serializingResponseObjectsOfType(final Class<X> type) {
        return mapper ->
                serializingResponseObjectsThat(areOfType(type))
                        .using((ResponseMapper<Object>) mapper);
    }

    /**
     * Configures {@link HttpMate} to throw an exception if no {@link ResponseMapper} configured under
     * {@link SerializationStage#serializingResponseObjectsThat(Predicate)},
     * {@link SerializationStage#serializingResponseObjectsOfType(Class)}, etc. matches the use case return value.
     *
     * @return the next step in the fluent builder
     */
    default T throwAnExceptionByDefault() {
        return serializingResponseObjectsByDefaultUsing(failWithMessage("No response mapper found"));
    }
}
