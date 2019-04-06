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

package com.envimate.httpmate.builder.configurators;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.HttpMateConfigurator;
import com.envimate.httpmate.builder.Using;
import com.envimate.httpmate.convenience.mappers.DefaultExceptionMapper;
import com.envimate.httpmate.mapper.ResponseMapper;
import com.envimate.httpmate.response.HttpResponse;

import java.util.function.Predicate;

import static com.envimate.httpmate.convenience.mappers.DefaultExceptionMapper.theDefaultExceptionMapper;
import static com.envimate.httpmate.convenience.mappers.ExceptionFilters.areOfType;

public interface ExceptionConfigurator {

    /**
     * Enters a fluent builder that configures a {@link ResponseMapper} that will be used to map a caught exception
     * to a {@link HttpResponse} if the caught exception matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link ResponseMapper} should be used
     *               on the respective exception
     * @return the next step in the fluent builder
     */
    Using<HttpMateConfigurator, ResponseMapper<Throwable>> mappingExceptionsThat(Predicate<Throwable> filter);

    /**
     * Configures the default {@link ResponseMapper} that will be used to map a caught exception
     * to a {@link HttpResponse} if no {@link ResponseMapper} configured under
     * {@link ExceptionConfigurator#mappingExceptionsThat(Predicate)},
     * {@link ExceptionConfigurator#mappingExceptionsOfType(Class)}, etc. matches the caught exception.
     *
     * @param mapper a {@link ResponseMapper}
     * @return the next step in the fluent builder
     */
    void mappingExceptionsByDefaultUsing(ResponseMapper<Throwable> mapper);

    /**
     * Enters a fluent builder that configures a {@link ResponseMapper} that will be used to map a caught exception
     * to a {@link HttpResponse} if the caught exception is of the specified type.
     *
     * @param type the type of exception that will be mapped by the {@link ResponseMapper}
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <X extends Throwable> Using<HttpMateConfigurator, ResponseMapper<X>> mappingExceptionsOfType(final Class<X> type) {
        return mapper ->
                mappingExceptionsThat(areOfType(type))
                        .using((object, metaData) -> mapper.map((X) object, metaData));
    }

    /**
     * Configures {@link HttpMate} to use the {@link DefaultExceptionMapper built-in default exception mapper}
     * to map a caught exception
     * to a {@link HttpResponse} if no {@link ResponseMapper} configured under
     * {@link ExceptionConfigurator#mappingExceptionsThat(Predicate)},
     * {@link ExceptionConfigurator#mappingExceptionsOfType(Class)}, etc. matches the caught exception.
     *
     * @return the next step in the fluent builder
     * @see DefaultExceptionMapper
     */
    default void usingTheBuiltInExceptionMapperByDefault() {
        mappingExceptionsByDefaultUsing(theDefaultExceptionMapper());
    }
}
