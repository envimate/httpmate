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

package com.envimate.httpmate.websockets;

public final class WebsocketChains {

    public static final String DETERMINE_WEBSOCKET_TYPE = "DETERMINE_WEBSOCKET_TYPE";

    public static final String WEBSOCKET_ESTABLISHMENT = "WEBSOCKET_ESTABLISHMENT";
    public static final String WEBSOCKET_OPEN = "WEBSOCKET_OPEN";
    public static final String WEBSOCKET_MESSAGE = "WEBSOCKET_MESSAGE";
    public static final String WEBSOCKET_CLOSE = "WEBSOCKET_CLOSE";
    public static final String WEBSOCKET_CLOSED = "WEBSOCKET_CLOSED";

    public static final String SEND_TO_WEBSOCKETS = "SEND_TO_WEBSOCKETS";

    private WebsocketChains() {
    }
}
