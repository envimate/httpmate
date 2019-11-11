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

package com.envimate.httpmate.logger;

import com.envimate.httpmate.chains.MetaData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.logger.LogLevel.*;
import static com.envimate.httpmate.logger.LogMessage.logMessage;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString(exclude = "metaData")
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Logger {
    private final LoggerImplementation loggerImplementation;
    private final MetaData metaData;

    public static Logger logger(final LoggerImplementation loggerImplementation,
                                final MetaData metaData) {
        validateNotNull(loggerImplementation, "loggerImplementation");
        validateNotNull(metaData, "metaData");
        return new Logger(loggerImplementation, metaData);
    }

    public void trace(final String message) {
        log(message, TRACE);
    }

    public void trace(final Throwable throwable) {
        log(throwable, TRACE);
    }

    public void trace(final Throwable throwable, final String message) {
        log(throwable, message, TRACE);
    }

    public void debug(final String message) {
        log(message, DEBUG);
    }

    public void debug(final Throwable throwable) {
        log(throwable, DEBUG);
    }

    public void debug(final Throwable throwable, final String message) {
        log(throwable, message, DEBUG);
    }

    public void info(final String message) {
        log(message, INFO);
    }

    public void info(final Throwable throwable) {
        log(throwable, INFO);
    }

    public void info(final Throwable throwable, final String message) {
        log(throwable, message, INFO);
    }

    public void warn(final String message) {
        log(message, WARN);
    }

    public void warn(final Throwable throwable) {
        log(throwable, WARN);
    }

    public void warn(final Throwable throwable, final String message) {
        log(throwable, message, WARN);
    }

    public void error(final String message) {
        log(message, ERROR);
    }

    public void error(final Throwable throwable) {
        log(throwable, ERROR);
    }

    public void error(final Throwable throwable, final String message) {
        log(throwable, message, ERROR);
    }

    public void fatal(final String message) {
        log(message, FATAL);
    }

    public void fatal(final Throwable throwable) {
        log(throwable, FATAL);
    }

    public void fatal(final Throwable throwable, final String message) {
        log(throwable, message, FATAL);
    }

    public void log(final String message, final LogLevel logLevel) {
        log(null, message, logLevel);
    }

    public void log(final Throwable throwable, final LogLevel logLevel) {
        log(throwable, null, logLevel);
    }

    public void log(final Throwable throwable, final String message, final LogLevel logLevel) {
        final LogMessage logMessage = logMessage(message, throwable, logLevel, metaData);
        loggerImplementation.log(logMessage);
    }
}
