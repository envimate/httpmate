package websockets.exampleproject.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Message {
    public final MessageId id;
    public final MessageContent content;
    public final Username sender;
    public final List<Username> recipients;

    public static Message message(final MessageId messageId,
                                  final MessageContent content,
                                  final Username sender,
                                  final List<Username> recipients) {
        validateNotNull(messageId, "messageId");
        validateNotNull(content, "content");
        validateNotNull(sender, "sender");
        validateNotNull(recipients, "recipients");
        return new Message(messageId, content, sender, recipients);
    }
}
