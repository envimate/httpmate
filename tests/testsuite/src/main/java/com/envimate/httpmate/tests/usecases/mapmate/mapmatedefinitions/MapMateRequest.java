package com.envimate.httpmate.tests.usecases.mapmate.mapmatedefinitions;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMateRequest {
    private final DataTransferObject dataTransferObject;

    public static MapMateRequest mapMateRequest(final DataTransferObject dataTransferObject) {
        validateNotNull(dataTransferObject, "dataTransferObject");
        return new MapMateRequest(dataTransferObject);
    }

    public DataTransferObject getDataTransferObject() {
        return dataTransferObject;
    }
}
