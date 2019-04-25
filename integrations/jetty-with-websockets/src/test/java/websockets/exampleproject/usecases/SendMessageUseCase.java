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

package websockets.exampleproject.usecases;

import websockets.exampleproject.domain.*;

import java.util.List;

import static com.envimate.messageMate.messageBus.EventType.eventTypeFromString;
import static websockets.exampleproject.Application.MESSAGE_BUS;
import static websockets.exampleproject.domain.MessageRepository.messageRepository;
import static websockets.exampleproject.usecases.events.NewMessageEvent.newMessageEvent;

public final class SendMessageUseCase {
    private final MessageRepository messageRepository = messageRepository();

    public Message send(final SendMessageRequest sendMessageRequest) {
        final MessageContent content = sendMessageRequest.getContent();
        final User sender = sendMessageRequest.getSender();
        final List<Username> recipients = sendMessageRequest.getReceivers();
        final Message message = messageRepository.addMessage(content, sender, recipients);
        MESSAGE_BUS.send(eventTypeFromString("NewMessage"), newMessageEvent(message));
        return message;
    }
}
