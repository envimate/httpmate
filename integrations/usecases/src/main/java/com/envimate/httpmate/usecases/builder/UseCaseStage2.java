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

@FunctionalInterface
public interface UseCaseStage2<T> {

    /**
     * Configures the http path on which the use case that was configured in
     * {@link UseCaseStage1#servingTheUseCase(Class)} will be served. The path may
     * contain parameter wildcards enclosed in angle brackets (i.e. '{@code <}' and '{@code >}').
     * You will be able to access these parameter wildcards later during request processing
     * indexed by the identifier you put
     * inside the angle brackets. Example:
     *
     * {@code "/foo/<wildcard>/bar"} would match requests to
     * <ul>
     * <li> {@code "/foo/anything/bar"}
     * <li> {@code "/foo/asdf/bar"}
     * </ul>
     * but not to
     * <ul>
     * <li> {@code "/foo"}
     * <li> {@code "/foo/anything/bar/asdf"}
     * <li> {@code "/foo/bar"}
     * </ul>
     * and the parameter will be accessible via the index {@link String} "wildcard".
     *
     * @param pathTemplate the path on which the use case will be served
     * @return the next step in the fluent builder
     */
    UseCaseStage3<T> forRequestPath(String pathTemplate);
}
