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

import com.envimate.httpmate.CoreModule;
import com.envimate.httpmate.chains.Configurator;

import static com.envimate.httpmate.chains.Configurator.configuratorForType;
import static com.envimate.httpmate.logger.Loggers.*;
import static com.envimate.httpmate.util.Validators.validateNotNull;

public final class LoggerConfigurators {

    private LoggerConfigurators() {
    }

    public static Configurator toLogUsing(final Logger logger) {
        validateNotNull(logger, "logger");
        return configuratorForType(CoreModule.class, coreModule -> coreModule.setLogger(logger));
    }

    public static Configurator toLogToStdout() {
        return toLogUsing(stdoutLogger());
    }

    public static Configurator toLogToStderr() {
        return toLogUsing(stderrLogger());
    }

    public static Configurator toLogToStdoutAndStderr() {
        return toLogUsing(stdoutAndStderrLogger());
    }

    public static Configurator toDropAllLogMessages() {
        return toLogUsing(noLogger());
    }
}
