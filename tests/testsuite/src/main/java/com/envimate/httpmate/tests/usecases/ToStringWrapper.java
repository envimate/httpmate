package com.envimate.httpmate.tests.usecases;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToStringWrapper {
    private final Object object;

    public static ToStringWrapper toStringWrapper(final Object object) {
        validateNotNull(object, "object");
        return new ToStringWrapper(object);
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
