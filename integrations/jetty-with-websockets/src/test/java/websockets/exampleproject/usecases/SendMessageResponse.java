package websockets.exampleproject.usecases;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SendMessageResponse {

    public static SendMessageResponse sendMessageResponse() {
        return new SendMessageResponse();
    }
}
