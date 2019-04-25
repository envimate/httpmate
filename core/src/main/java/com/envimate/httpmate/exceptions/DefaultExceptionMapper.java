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

package com.envimate.httpmate.exceptions;

import com.envimate.httpmate.chains.MetaData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.envimate.httpmate.HttpMateChainKeys.LOGGER;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_STATUS;
import static com.envimate.httpmate.http.Http.StatusCodes.INTERNAL_SERVER_ERROR;

/**
 * A {@link ExceptionMapper} that will map an exception to an with an empty body
 * and the status code 500 (Internal Server Error). It will log the stack trace of the exception.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

    public static ExceptionMapper<Throwable> theDefaultExceptionMapper() {
        return new DefaultExceptionMapper();
    }

    @Override
    public void map(final Throwable exception, final MetaData metaData) {
        final String stackTrace = stackTraceToString(exception);
        metaData.get(LOGGER).log(stackTrace, metaData);
        metaData.set(RESPONSE_STATUS, INTERNAL_SERVER_ERROR);
    }

    private static String stackTraceToString(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
