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

import static java.lang.String.format;

public final class UnsupportedTargetTypeException extends RuntimeException {

    private UnsupportedTargetTypeException(final String message) {
        super(message);
    }

    public static UnsupportedTargetTypeException unsupportedTargetTypeException(final Class<?> expectedType,
                                                                                final Class<?> actualType) {
        final String message = format("Can only deserialize response objects to %s but found: %s",
                expectedType.getName(), actualType.getName());
        return new UnsupportedTargetTypeException(message);
    }
}
