package websockets.exampleproject.usecases;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import websockets.exampleproject.domain.MessageContent;
import websockets.exampleproject.domain.User;
import websockets.exampleproject.domain.Username;

import java.util.List;

import static com.envimate.httpmate.util.Validators.validateArrayNeitherNullNorEmptyNorContainsNull;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.asList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SendMessageRequest {
    private final List<Username> receivers;
    private final User sender;
    private final MessageContent content;

    public static SendMessageRequest sendMessageRequest(final Username[] receivers,
                                                        final User sender,
                                                        final MessageContent content) {
        validateArrayNeitherNullNorEmptyNorContainsNull(receivers, "receivers");
        validateNotNull(sender, "sender");
        validateNotNull(content, "content");
        return new SendMessageRequest(asList(receivers), sender, content);
    }

    public MessageContent getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }

    public List<Username> getReceivers() {
        return receivers;
    }
}
