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

import java.io.PrintStream;

public final class Loggers {

    private Loggers() {
    }

    public static LoggerImplementation stderrLogger() {
        return logTo(System.err);
    }

    public static LoggerImplementation stdoutLogger() {
        return logTo(System.out);
    }

    public static LoggerImplementation logTo(final PrintStream printStream) {
        return message -> writeTo(printStream, message);
    }

    public static LoggerImplementation stdoutAndStderrLogger() {
        return message -> {
            if (message.hasException()) {
                writeTo(System.err, message);
            } else {
                writeTo(System.out, message);
            }
        };
    }

    public static LoggerImplementation noLogger() {
        return message -> {
        };
    }

    private static void writeTo(final PrintStream printStream, final LogMessage message) {
        final String formattedMessage = message.formattedMessage();
        printStream.println(formattedMessage);
    }
}
