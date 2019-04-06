package websockets.exampleproject.usecases.login;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import websockets.exampleproject.domain.Password;
import websockets.exampleproject.domain.Username;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoginRequest {
    private final Username username;
    private final Password password;

    public static LoginRequest loginRequest(final Username username,
                                            final Password password) {
        validateNotNull(username, "username");
        validateNotNull(password, "password");
        return new LoginRequest(username, password);
    }
}
