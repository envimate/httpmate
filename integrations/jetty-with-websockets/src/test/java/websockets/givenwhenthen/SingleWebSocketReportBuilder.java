package websockets.givenwhenthen;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleWebSocketReportBuilder {
    private final List<String> receivedFrames = new LinkedList<>();
    private boolean closed = false;

    public static SingleWebSocketReportBuilder singleWebSocketReportBuilder() {
        return new SingleWebSocketReportBuilder();
    }

    void reportReceivedFrame(final String content) {
        receivedFrames.add(content);
    }

    void reportClosed() {
        closed = true;
    }

    boolean didReceive(final String message) {
        return receivedFrames.contains(message);
    }

    boolean wasClosed() {
        return closed;
    }
}
