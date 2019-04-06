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

package com.envimate.httpmate.chains;

public final class HttpMateChains {

    private HttpMateChains() {
    }

    public static final String PRE_PROCESS = "PRE_PROCESS";
    public static final String PROCESS_HEADERS = "PROCESS_HEADERS";
    public static final String PROCESS_BODY = "PROCESS_BODY";
    public static final String PROCESS_SINGLE_PART = "PROCESS_SINGLE_PART";
    public static final String PROCESS_BODY_STRING = "PROCESS_BODY_STRING";
    public static final String PRE_DETERMINE_EVENT = "PRE_DETERMINE_EVENT";
    public static final String DETERMINE_EVENT = "DETERMINE_EVENT";
    public static final String AUTHENTICATION = "AUTHENTICATION";
    public static final String AUTHORIZATION = "AUTHORIZATION";
    public static final String PRE_MAP_TO_EVENT = "PRE_MAP_TO_EVENT";
    public static final String MAP_TO_EVENT = "MAP_TO_EVENT";
    public static final String SUBMIT = "SUBMIT";
    public static final String PRE_SERIALIZATION = "PRE_SERIALIZATION";
    public static final String SERIALIZATION = "SERIALIZATION";
    public static final String POST_SERIALIZATION = "POST_SERIALIZATION";
    public static final String EXCEPTION_OCCURRED = "EXCEPTION_OCCURRED";
}
