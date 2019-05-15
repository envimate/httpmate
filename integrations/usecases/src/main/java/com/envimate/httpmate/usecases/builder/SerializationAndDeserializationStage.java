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

package com.envimate.httpmate.usecases.builder;

import com.envimate.httpmate.events.builder.Using;
import com.envimate.httpmate.usecases.EventFilter;
import com.envimate.httpmate.usecases.usecase.SerializerAndDeserializer;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestFilters;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestMapper;
import com.envimate.messageMate.useCaseAdapter.mapping.ResponseFilters;
import com.envimate.messageMate.useCaseAdapter.mapping.ResponseMapper;

import java.util.function.Predicate;

import static com.envimate.httpmate.usecases.usecase.DelegatingDeserializerAndSerializer.delegatingDeserializerAndSerializer;
import static com.envimate.messageMate.useCaseAdapter.mapping.RequestFilters.areOfType;

public interface SerializationAndDeserializationStage<T> {

    <X> Using<SerializationAndDeserializationStage<T>, RequestMapper<X>> mappingUseCaseParametersThat(EventFilter<?> filter);

    Using<SerializationAndDeserializationStage<T>, ResponseMapper<Object>> serializingResponseObjectsThat(
            Predicate<Object> filter);

    T mappingRequestsAndResponsesUsing(SerializerAndDeserializer serializerAndDeserializer);

    @SuppressWarnings("unchecked")
    default <X> Using<SerializationAndDeserializationStage<T>, RequestMapper<X>> mappingUseCaseParametersOfType(
            final Class<X> type) {
        return mappingUseCaseParametersThat((clazz, event) -> areOfType(type).test((Class<X>) clazz, event));
    }

    default T throwAnExceptionByDefault() {
        return mappingRequestsAndResponsesUsing(delegatingDeserializerAndSerializer(
                RequestFilters.failWithMessage("No request mapper found"),
                ResponseFilters.failWithMessage("No response mapper found")));
    }

    @SuppressWarnings("unchecked")
    default <X> Using<SerializationAndDeserializationStage<T>, ResponseMapper<X>>
    serializingResponseObjectsOfType(final Class<X> type) {
        return mapper ->
                serializingResponseObjectsThat(ResponseFilters.areOfType(type))
                        .using((ResponseMapper<Object>) mapper);
    }
}
