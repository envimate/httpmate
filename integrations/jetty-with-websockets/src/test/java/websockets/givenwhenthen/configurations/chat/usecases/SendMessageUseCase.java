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

import websockets.givenwhenthen.configurations.chat.domain.MessageContent;
import websockets.givenwhenthen.configurations.chat.domain.User;
import websockets.givenwhenthen.configurations.chat.domain.UserRepository;
import websockets.givenwhenthen.configurations.chat.domain.Username;

import java.util.Map;

import static websockets.givenwhenthen.configurations.chat.ChatConfiguration.messageBus;
import static websockets.givenwhenthen.configurations.chat.domain.UserRepository.userRepository;
import static websockets.givenwhenthen.configurations.chat.usecases.NewMessageEvent.newMessageEvent;

public final class SendMessageUseCase {
    private final UserRepository userRepository = userRepository();

    public void sendMessage(final ChatMessage message) {
        final Username recipientName = message.recipient();
        final User recipient = userRepository.byName(recipientName);
        final MessageContent content = message.content();

        final NewMessageEvent event = newMessageEvent(recipient, content);

        final String contentString = event.content().toString();
        final String recipientString = event.recipient().name().internalValueForMapping();
        final Map<String, Object> e = Map.of("content", contentString, "recipient", recipientString);

        messageBus.send("NewMessageEvent", e);
    }
}
