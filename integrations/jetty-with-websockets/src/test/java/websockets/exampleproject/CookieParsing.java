package websockets.exampleproject;

import java.util.Optional;

import static java.util.Arrays.stream;

public final class CookieParsing {

    public static Optional<String> getCookie(final String key,
                                             final String cookieHeader) {
        return stream(cookieHeader.split("; "))
                .map(token -> token.split("="))
                .filter(array -> array[0].equals(key))
                .map(array -> array[1])
                .findFirst();
    }
}
