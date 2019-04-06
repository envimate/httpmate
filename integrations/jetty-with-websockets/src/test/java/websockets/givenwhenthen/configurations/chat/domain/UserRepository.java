package websockets.givenwhenthen.configurations.chat.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static java.util.Arrays.asList;
import static websockets.givenwhenthen.configurations.chat.domain.User.user;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserRepository {
    private static final List<User> users = asList(user("maus"), user("elefant"), user("ente"));

    public static UserRepository userRepository() {
        return new UserRepository();
    }

    public User byName(final Username username) {
        return users.stream()
                .filter(user -> user.name().equals(username))
                .findAny()
                .orElseThrow(() -> new RuntimeException("No user with name"));
    }
}
