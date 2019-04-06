package websockets.givenwhenthen;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import static websockets.givenwhenthen.SingleWebSocketReportBuilder.singleWebSocketReportBuilder;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReportBuilder {
    final List<Exception> exceptionsDuringWebSocketConnecting = new LinkedList<>();
    private final List<SingleWebSocketReportBuilder> webSocketReporters = new LinkedList<>();
    String normalResponseBody;
    Integer numberOfActiveWebSockets;

    static ReportBuilder reportBuilder() {
        return new ReportBuilder();
    }

    public void reportExceptionExceptionDuringWebSocketConnecting(final Exception e) {
        exceptionsDuringWebSocketConnecting.add(e);
    }

    public SingleWebSocketReportBuilder reportNewWebSocket() {
        final SingleWebSocketReportBuilder singleWebSocketReportBuilder = singleWebSocketReportBuilder();
        webSocketReporters.add(singleWebSocketReportBuilder);
        return singleWebSocketReportBuilder;
    }

    public void reportNormalResponseBody(final String body) {
        this.normalResponseBody = body;
    }

    public List<SingleWebSocketReportBuilder> getWebSocketReporters() {
        return webSocketReporters;
    }

    public void reportNumberOfActiveWebSockets(final int numberOfActiveWebSockets) {
        this.numberOfActiveWebSockets = numberOfActiveWebSockets;
    }
}
