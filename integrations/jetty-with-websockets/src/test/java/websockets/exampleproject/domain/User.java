package websockets.exampleproject.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class User {
    private final Username name;
    private final Password password;

    public static User user(final Username name,
                            final Password password) {
        validateNotNull(name, "name");
        validateNotNull(password, "password");
        return new User(name, password);
    }

    public Username username() {
        return name;
    }

    public Password password() {
        return password;
    }
}
