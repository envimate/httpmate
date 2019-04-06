package com.envimate.httpmate.multipart;

import com.envimate.httpmate.chains.rules.Processor;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.request.ContentType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;

import static com.envimate.httpmate.chains.HttpMateChainKeys.CONTENT_TYPE;
import static com.envimate.httpmate.chains.HttpMateChainKeys.BODY_STREAM;
import static com.envimate.httpmate.multipart.MULTIPART_CHAIN_KEYS.MULTIPART_ITERATOR_BODY;
import static com.envimate.httpmate.multipart.MultipartHandler.parse;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartProcessor implements Processor {

    public static Processor multipartProcessor() {
        return new MultipartProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        final InputStream body = metaData.get(BODY_STREAM);
        final ContentType contentType = metaData.get(CONTENT_TYPE);
        final MultipartIteratorBody multipartIteratorBody = parse(body, contentType);
        metaData.set(MULTIPART_ITERATOR_BODY, multipartIteratorBody);
    }
}
