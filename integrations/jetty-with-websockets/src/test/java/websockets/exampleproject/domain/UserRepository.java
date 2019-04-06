package websockets.exampleproject.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static websockets.exampleproject.domain.Password.password;
import static websockets.exampleproject.domain.User.user;
import static websockets.exampleproject.domain.Username.username;

public final class UserRepository {
    private static final Map<Username, User> USERS = new HashMap<>();

    static {
        addUser("alex", "a");
        addUser("richard", "r");
        addUser("marco", "m");
    }

    private static void addUser(final String name,
                                final String password) {
        final Username username = username(name);
        USERS.put(username, user(username, password(password)));
    }

    public static UserRepository userRepository() {
        return new UserRepository();
    }

    public Optional<User> getIfCorrectAuthenticationInformation(final String username,
                                                                final String password) {
        final Username key = username(username);
        if(USERS.containsKey(key)) {
            final User user = USERS.get(key);
            if (user.password().matches(password(password))) {
                return of(user);
            }
        }
        return empty();
    }
}
