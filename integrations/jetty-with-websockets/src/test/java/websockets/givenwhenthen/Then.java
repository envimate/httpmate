package websockets.givenwhenthen;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseA;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseB;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseC;
import websockets.givenwhenthen.configurations.artificial.usecases.count.CountUseCase;

import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Then {
    private final ReportBuilder reportBuilder;
    private final When when;

    static Then then(final ReportBuilder reportBuilder,
                     final When when) {
        return new Then(reportBuilder, when);
    }

    public Then allWebSocketConnectionsCouldBeEstablishedSuccessfully() {
        assertThat(reportBuilder.exceptionsDuringWebSocketConnecting.isEmpty(), is(true));
        return this;
    }

    public Then theWebSocketConnectionCouldNotBeEstablished() {
        assertThat(reportBuilder.exceptionsDuringWebSocketConnecting.isEmpty(), is(false));
        return this;
    }

    public Then theResponseBodyWas(final String content) {
        assertThat(reportBuilder.normalResponseBody, is(content));
        return this;
    }

    public Then theCounterOfTheCountUseCaseWas(final int expectedCount) {
        assertThat(CountUseCase.COUNTER.getValue(), is(expectedCount));
        return this;
    }

    public Then useCaseAHasBeenInvoked() {
        assertThat(UseCaseA.HAS_BEEN_INVOKED.getValue(), is(true));
        return this;
    }

    public Then useCaseBHasBeenInvoked() {
        assertThat(UseCaseB.HAS_BEEN_INVOKED.getValue(), is(true));
        return this;
    }

    public Then useCaseCHasNotBeenInvoked() {
        assertThat(UseCaseC.HAS_BEEN_INVOKED.getValue(), is(false));
        return this;
    }

    public Then exactlyOneClientHasBeenClosed() {
        assertThat(numberOfDifferentWebSocketsThatHaveBeenReportedAsClosedOnClientSide(), is(1L));
        return this;
    }

    public Then exactlyOneWebSocketReceivedMessage(final String expectedReceivedMessage) {
        return exactlyNDifferentWebSocketsReceivedTheMessage(1, expectedReceivedMessage);
    }

    public Then exactlyTwoDifferentWebSocketsReceivedTheMessage(final String expectedReceivedMessage) {
        return exactlyNDifferentWebSocketsReceivedTheMessage(2, expectedReceivedMessage);
    }

    public Then theQueriedNumberOfActiveConnectionsWas(final int expected) {
        assertThat(reportBuilder.numberOfActiveWebSockets, is(expected));
        return this;
    }

    private Then exactlyNDifferentWebSocketsReceivedTheMessage(long n, final String expectedReceivedMessage) {
        assertThat(numberOfDifferentWebSocketsThatReceived(expectedReceivedMessage), is(n));
        return this;
    }

    private long numberOfDifferentWebSocketsThatReceived(final String expectedReceivedMessage) {
        return numberOfDifferentWebSocketsThat(report -> report.didReceive(expectedReceivedMessage));
    }

    private long numberOfDifferentWebSocketsThatHaveBeenReportedAsClosedOnClientSide() {
        return numberOfDifferentWebSocketsThat(SingleWebSocketReportBuilder::wasClosed);
    }

    private long numberOfDifferentWebSocketsThat(final Predicate<SingleWebSocketReportBuilder> predicate) {
        return reportBuilder.getWebSocketReporters().stream()
                .filter(predicate)
                .count();
    }

    public When andWhen() {
        return when;
    }
}
