package websockets.givenwhenthen;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WhenOrThen {
    private final When when;
    private final ReportBuilder reportBuilder;

    static WhenOrThen whenOrThen(final When when, final ReportBuilder reportBuilder) {
        return new WhenOrThen(when, reportBuilder);
    }

    public When andWhen() {
        return when;
    }

    public Then then() {
        return Then.then(reportBuilder, when);
    }
}
