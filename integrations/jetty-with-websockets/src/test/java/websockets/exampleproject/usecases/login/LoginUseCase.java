package websockets.exampleproject.usecases.login;

import websockets.exampleproject.domain.UserRepository;

public final class LoginUseCase {
    private final UserRepository userRepository = new UserRepository();

    public void login(final LoginRequest loginRequest) {
        System.out.println("login request");
    }
}
