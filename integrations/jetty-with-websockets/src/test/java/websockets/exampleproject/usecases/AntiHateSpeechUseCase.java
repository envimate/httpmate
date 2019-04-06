package websockets.exampleproject.usecases;

import websockets.exampleproject.domain.Username;

import java.util.List;

import static com.envimate.messageMate.messageBus.EventType.eventTypeFromString;
import static java.util.Arrays.asList;
import static websockets.exampleproject.Application.MESSAGE_BUS;
import static websockets.exampleproject.usecases.BanUserEvent.banUserEvent;

public final class AntiHateSpeechUseCase {
    private static final List<String> BLACKLIST = asList("doof", "Blödmann", "Arsch");

    public static void register() {
        MESSAGE_BUS.subscribe(eventTypeFromString("SendMessageRequest"), o -> {
            final SendMessageRequest sendMessageRequest = (SendMessageRequest) o;
            final String content = sendMessageRequest.getContent().stringValue();
            final boolean hit = BLACKLIST.stream()
                    .map(content::contains)
                    .findFirst()
                    .orElse(false);
            if (hit) {
                final Username username = sendMessageRequest.getSender().username();
                MESSAGE_BUS.send(eventTypeFromString("BanUser"), banUserEvent(username));
            }
        });
    }
}
