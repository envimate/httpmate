package websockets.givenwhenthen.configurations.chat.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static websockets.givenwhenthen.configurations.chat.domain.Username.username;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class User {
    private final Username name;

    public static User user(final String name) {
        return new User(username(name));
    }

    public Username name() {
        return name;
    }
}
