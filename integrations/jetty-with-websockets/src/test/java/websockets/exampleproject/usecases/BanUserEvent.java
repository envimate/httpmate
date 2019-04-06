package websockets.exampleproject.usecases;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import websockets.exampleproject.domain.Username;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BanUserEvent {
    private final Username username;

    public static BanUserEvent banUserEvent(final Username username) {
        validateNotNull(username, "username");
        return new BanUserEvent(username);
    }

    public Username username() {
        return username;
    }
}
