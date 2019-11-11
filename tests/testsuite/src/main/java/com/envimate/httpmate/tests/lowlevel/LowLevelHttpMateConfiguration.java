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

package com.envimate.httpmate.tests.lowlevel;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.logger.LoggerImplementation;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.debug.DebugConfigurator.toBeInDebugMode;
import static com.envimate.httpmate.http.HttpRequestMethod.*;
import static com.envimate.httpmate.logger.LoggerConfigurators.toLogUsing;
import static com.envimate.httpmate.tests.lowlevel.handlers.ContentTypeInResponseHandler.contentTypeInResponseHandler;
import static com.envimate.httpmate.tests.lowlevel.handlers.EchoBodyHandler.echoBodyHandler;
import static com.envimate.httpmate.tests.lowlevel.handlers.EchoContentTypeHandler.echoContentTypeHandler;
import static com.envimate.httpmate.tests.lowlevel.handlers.ExceptionThrowingHandler.exceptionThrowingHandler;
import static com.envimate.httpmate.tests.lowlevel.handlers.HeadersInResponseHandler.headersInResponseHandler;
import static com.envimate.httpmate.tests.lowlevel.handlers.LogHandler.logHandler;
import static com.envimate.httpmate.tests.lowlevel.handlers.MyDownloadHandler.downloadHandler;

public final class LowLevelHttpMateConfiguration {
    public static StringBuilder logger;

    private LowLevelHttpMateConfiguration() {
    }

    public static HttpMate theLowLevelHttpMateInstanceUsedForTesting() {
        logger = new StringBuilder();
        return anHttpMate()
                .serving(echoBodyHandler())
                .forRequestPath("/echo").andRequestMethods(GET, POST, PUT, DELETE)
                .get("echo_contenttype", echoContentTypeHandler())
                .get("/set_contenttype_in_response", contentTypeInResponseHandler())
                .get("/headers_response", headersInResponseHandler())
                .get("/log", logHandler())
                .get("/download", downloadHandler())
                .get("/exception", exceptionThrowingHandler())
                .configured(toLogUsing(logger()))
                .configured(toBeInDebugMode())
                .build();
    }

    private static LoggerImplementation logger() {
        return logMessage -> {
            final String formattedMessage = logMessage.formattedMessage();
            logger.append(formattedMessage);
        };
    }
}
