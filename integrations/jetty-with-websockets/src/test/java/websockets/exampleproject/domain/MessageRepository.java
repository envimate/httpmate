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

package websockets.exampleproject.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static websockets.exampleproject.domain.Message.message;
import static websockets.exampleproject.domain.MessageId.randomMessageId;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageRepository {
    private static final Map<MessageId, Message> MESSAGES = new HashMap<>();

    public static MessageRepository messageRepository() {
        return new MessageRepository();
    }

    public Message addMessage(final MessageContent content,
                              final User user,
                              final List<Username> recipients) {
        validateNotNull(content, "content");
        final MessageId id = randomMessageId();
        final Message message = message(id, content, user.username(), recipients);
        MESSAGES.put(id, message);
        return message;
    }
}
