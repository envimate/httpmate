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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.StringJoiner;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogMessage {
    private final String message;
    private final Throwable throwable;
    private final LogLevel logLevel;
    private final MetaData metaData;

    public static LogMessage logMessage(final String message,
                                        final Throwable throwable,
                                        final LogLevel logLevel,
                                        final MetaData metaData) {
        validateNotNull(logLevel, "logLevel");
        validateNotNull(metaData, "metaData");
        return new LogMessage(message, throwable, logLevel, metaData);
    }

    public boolean hasMessage() {
        return message().isPresent();
    }

    public Optional<String> message() {
        return ofNullable(message);
    }

    public boolean hasException() {
        return exception().isPresent();
    }

    public Optional<Throwable> exception() {
        return ofNullable(throwable);
    }

    public Optional<String> stacktraceOfException() {
        return exception().map(LogMessage::stackTraceToString);
    }

    public String formattedMessage() {
        final String prefix = logLevel.asString() + ": ";
        final StringJoiner joiner = new StringJoiner("\n", prefix, "");
        message().ifPresent(joiner::add);
        stacktraceOfException().ifPresent(joiner::add);
        return joiner.toString();
    }

    public LogLevel logLevel() {
        return logLevel;
    }

    public MetaData metaData() {
        return metaData;
    }

    private static String stackTraceToString(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
