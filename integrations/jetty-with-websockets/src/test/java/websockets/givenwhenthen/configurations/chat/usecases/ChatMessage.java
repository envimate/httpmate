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

package websockets.givenwhenthen.configurations.chat.usecases;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import websockets.givenwhenthen.configurations.chat.domain.MessageContent;
import websockets.givenwhenthen.configurations.chat.domain.Username;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatMessage {
    private final MessageContent content;
    private final Username recipient;

    public static ChatMessage chatMessage(final MessageContent content,
                                          final Username recipient) {
        return new ChatMessage(content, recipient);
    }

    public MessageContent content() {
        return content;
    }

    public Username recipient() {
        return recipient;
    }
}
