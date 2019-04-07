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
import com.envimate.messageMate.useCaseAdapter.mapping.RequestMapper;

import java.util.Map;
import java.util.function.BiPredicate;

import static com.envimate.messageMate.useCaseAdapter.mapping.RequestFilters.areOfType;
import static com.envimate.messageMate.useCaseAdapter.mapping.RequestFilters.failWithMessage;

public interface DeserializationStage<T> {

    /**
     * Enters a fluent builder that configures a {@link RequestMapper} that will be used to deserialize a
     * to a use case parameter if the http request matches the provided {@link BiPredicate filter}.
     *
     * @param filter a {@link BiPredicate} that returns true if the {@link RequestMapper} should be used
     *               on the respective http request
     * @return the next step in the fluent builder
     */
    <X> Using<DeserializationStage<T>, RequestMapper<X>> mappingUseCaseParametersThat(
            BiPredicate<Class<?>, Map<String, Object>> filter);

    /**
     * Configures the default {@link RequestMapper} that will be used to deserialize a
     * to a use case parameter if no {@link RequestMapper} configured under
     * {@link DeserializationStage#mappingUseCaseParametersThat(BiPredicate)},
     * {@link DeserializationStage#mappingUseCaseParametersOfType(Class)}, etc. matches the request.
     *
     * @param mapper a {@link RequestMapper}
     * @return the next step in the fluent builder
     */
    T mappingUseCaseParametersByDefaultUsing(RequestMapper<Object> mapper);

    /**
     * Enters a fluent builder that configures a {@link RequestMapper} that will be used to deserialize a
     * to a case parameter if the use case parameter is of the specified type.
     *
     * @param type the type of use case parameters that will be deserialized by the {@link RequestMapper}
     * @return the next step in the fluent builder
     */
    default <X> Using<DeserializationStage<T>, RequestMapper<X>> mappingUseCaseParametersOfType(final Class<X> type) {
        return mappingUseCaseParametersThat(areOfType(type));
    }

    /**
     * Configures {@link HttpMate} to throw an exception if no {@link RequestMapper} configured under
     * {@link DeserializationStage#mappingUseCaseParametersThat(BiPredicate)},
     * {@link DeserializationStage#mappingUseCaseParametersOfType(Class)} (Class)}, etc. matches the request.
     *
     * @return the next step in the fluent builder
     */
    default T throwAnExceptionByDefault() {
        return mappingUseCaseParametersByDefaultUsing(failWithMessage("No request mapper found"));
    }
}
