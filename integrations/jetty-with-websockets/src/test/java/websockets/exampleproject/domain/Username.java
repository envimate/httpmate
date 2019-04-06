package websockets.exampleproject.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Username {
    private final String name;

    public static Username username(final String name) {
        validateNotNullNorEmpty(name, "name");
        return new Username(name);
    }

    public String stringValue() {
        return name;
    }
}
