package websockets.exampleproject.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Password {
    private final long hash;

    public static Password password(final String password) {
        validateNotNullNorEmpty(password, "password");
        return new Password(password.hashCode());
    }

    public boolean matches(final Password other) {
        validateNotNull(other, "other");
        return hash == other.hash;
    }
}
