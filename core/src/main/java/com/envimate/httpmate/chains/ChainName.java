package com.envimate.httpmate.chains;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChainName {
    private final String name;

    public static ChainName chainName(final String name) {
        validateNotNullNorEmpty(name, "name");
        return new ChainName(name);
    }

    public String name() {
        return name;
    }
}
