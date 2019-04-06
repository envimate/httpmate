package websockets.exampleproject.usecases;

import com.envimate.messageMate.messageBus.EventType;
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
