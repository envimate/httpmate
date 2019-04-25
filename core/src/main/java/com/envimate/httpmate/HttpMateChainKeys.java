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

package com.envimate.httpmate;

import com.envimate.httpmate.chains.MetaDataKey;
import com.envimate.httpmate.handler.Handler;
import com.envimate.httpmate.logger.Logger;
import com.envimate.httpmate.path.Path;
import com.envimate.httpmate.http.*;

import java.io.InputStream;
import java.util.Map;

import static com.envimate.httpmate.chains.MetaDataKey.metaDataKey;

public final class HttpMateChainKeys {

    private HttpMateChainKeys() {
    }

    public static final MetaDataKey<Throwable> EXCEPTION = metaDataKey("EXCEPTION");

    public static final MetaDataKey<Object> AUTHENTICATION_INFORMATION = metaDataKey("AUTHENTICATION_INFORMATION");
    public static final MetaDataKey<Logger> LOGGER = metaDataKey("LOGGER");

    public static final MetaDataKey<Handler> HANDLER = metaDataKey("HANDLER");

    public static final MetaDataKey<Boolean> IS_HTTP_REQUEST = metaDataKey("IS_HTTP_REQUEST");

    public static final MetaDataKey<Map<String, String>> RAW_HEADERS = metaDataKey("RAW_HEADERS");
    public static final MetaDataKey<Map<String, String>> RAW_QUERY_PARAMETERS = metaDataKey("RAW_QUERY_PARAMETERS");
    public static final MetaDataKey<String> RAW_METHOD = metaDataKey("RAW_METHOD");
    public static final MetaDataKey<String> RAW_PATH = metaDataKey("RAW_PATH");
    public static final MetaDataKey<Path> PATH = metaDataKey("PATH");
    public static final MetaDataKey<PathParameters> PATH_PARAMETERS = metaDataKey("PATH_PARAMETERS");
    public static final MetaDataKey<QueryParameters> QUERY_PARAMETERS = metaDataKey("QUERY_PARAMETERS");
    public static final MetaDataKey<HttpRequestMethod> METHOD = metaDataKey("METHOD");
    public static final MetaDataKey<InputStream> BODY_STREAM = metaDataKey("BODY_STREAM");
    public static final MetaDataKey<String> BODY_STRING = metaDataKey("BODY_STRING");
    public static final MetaDataKey<Map<String, Object>> BODY_MAP = metaDataKey("BODY_MAP");
    public static final MetaDataKey<Headers> HEADERS = metaDataKey("HEADERS");
    public static final MetaDataKey<ContentType> CONTENT_TYPE = metaDataKey("CONTENT_TYPE");

    public static final MetaDataKey<String> STRING_RESPONSE = metaDataKey("STRING_RESPONSE");
    public static final MetaDataKey<InputStream> STREAM_RESPONSE = metaDataKey("STREAM_RESPONSE");
    public static final MetaDataKey<Integer> RESPONSE_STATUS = metaDataKey("RESPONSE_STATUS");
    public static final MetaDataKey<Map<String, String>> RESPONSE_HEADERS = metaDataKey("RESPONSE_HEADERS");
}
