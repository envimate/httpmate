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
import com.envimate.httpmate.Logger;

import static com.envimate.httpmate.convenience.logger.Loggers.*;

public interface LoggerConfigurator {

    /**
     * Configures the {@link Logger} that {@link HttpMate} will use for logging.
     *
     * @param logger the logger
     * @return the next step in the fluent builder
     */
    void usingLogger(Logger logger);

    /**
     * Configures {@link HttpMate} drop all logged messages.
     *
     * @return the next step in the fluent builder
     *
     * @see LoggerConfigurator#usingLogger(Logger)
     * @see Logger
     */
    default void notLogging() {
        usingLogger(noLogger());
    }

    /**
     * Configures {@link HttpMate} to print all logged messages to stderr.
     *
     * @return the next step in the fluent builder
     *
     * @see LoggerConfigurator#usingLogger(Logger)
     * @see Logger
     */
    default void loggingToStderr() {
        usingLogger(stderrLogger());
    }

    /**
     * Configures {@link HttpMate} to print all logged messages to stdout.
     *
     * @return the next step in the fluent builder
     *
     * @see LoggerConfigurator#usingLogger(Logger)
     * @see Logger
     */
    default void loggingToStdout() {
        usingLogger(stdoutLogger());
    }

    /**
     * Configures {@link HttpMate} to print all logged messages to stdout and stderr.
     *
     * @return the next step in the fluent builder
     *
     * @see LoggerConfigurator#usingLogger(Logger)
     * @see Logger
     */
    default void loggingToStdoutAndStderr() {
        usingLogger(stdoutAndStderrLogger());
    }
}
