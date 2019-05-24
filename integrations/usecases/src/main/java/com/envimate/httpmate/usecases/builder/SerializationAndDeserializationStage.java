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
import com.envimate.messageMate.mapping.Demapifier;
import com.envimate.messageMate.mapping.Mapifier;
import com.envimate.messageMate.mapping.SerializationFilters;

import java.util.function.Predicate;

import static com.envimate.httpmate.usecases.usecase.DelegatingDeserializerAndSerializer.delegatingDeserializerAndSerializer;
import static com.envimate.messageMate.mapping.DeserializationFilters.areOfType;
import static com.envimate.messageMate.mapping.MissingDeserializationException.missingDeserializationException;
import static com.envimate.messageMate.mapping.MissingSerializationException.missingSerializationException;
import static java.lang.String.format;

public interface SerializationAndDeserializationStage<T> {

    <X> Using<SerializationAndDeserializationStage<T>, Demapifier<X>> mappingUseCaseParametersThat(EventFilter<?> filter);

    Using<SerializationAndDeserializationStage<T>, Mapifier<Object>> serializingResponseObjectsThat(
            Predicate<Object> filter);

    T mappingRequestsAndResponsesUsing(SerializerAndDeserializer serializerAndDeserializer);

    @SuppressWarnings("unchecked")
    default <X> Using<SerializationAndDeserializationStage<T>, Demapifier<X>> mappingUseCaseParametersOfType(
            final Class<X> type) {
        return mappingUseCaseParametersThat((clazz, event) -> areOfType(type).test((Class<X>) clazz, event));
    }

    default T throwAnExceptionByDefault() {
        return mappingRequestsAndResponsesUsing(delegatingDeserializerAndSerializer(
                (targetType, map) -> {
                    throw missingDeserializationException(format("No deserialization found for type %s", targetType));
                }, object -> {
                    throw missingSerializationException(format("No serialization found for object %s", object));
                }));
    }

    @SuppressWarnings("unchecked")
    default <X> Using<SerializationAndDeserializationStage<T>, Mapifier<X>>
    serializingResponseObjectsOfType(final Class<X> type) {
        return mapper ->
                serializingResponseObjectsThat(SerializationFilters.areOfType(type))
                        .using((Mapifier<Object>) mapper);
    }
}
