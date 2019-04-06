package websockets.givenwhenthen.configurations.chat.usecases;

import websockets.givenwhenthen.configurations.chat.domain.MessageContent;
import websockets.givenwhenthen.configurations.chat.domain.User;
import websockets.givenwhenthen.configurations.chat.domain.UserRepository;
import websockets.givenwhenthen.configurations.chat.domain.Username;

import java.util.Map;

import static websockets.givenwhenthen.configurations.chat.ChatConfiguration.MESSAGE_BUS;
import static websockets.givenwhenthen.configurations.chat.domain.UserRepository.userRepository;
import static websockets.givenwhenthen.configurations.chat.usecases.NewMessageEvent.newMessageEvent;

public final class SendMessageUseCase {
    private final UserRepository userRepository = userRepository();

    public void sendMessage(final ChatMessage message) {
        final Username recipientName = message.recipient();
        final User recipient = userRepository.byName(recipientName);
        final MessageContent content = message.content();


        final NewMessageEvent event = newMessageEvent(recipient, content);

        final Map<String, Object> e = Map.of("content", event.content().toString(), "recipient", event.recipient().name().internalValueForMapping());
        MESSAGE_BUS.send("NewMessageEvent", e);
    }
}
