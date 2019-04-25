/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.httpmate.multipart;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static com.envimate.httpmate.multipart.MultipartException.multipartException;
import static com.envimate.httpmate.multipart.MultipartPart.multipartFile;
import static com.envimate.httpmate.multipart.MultipartPart.multipartFormControl;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartIteratorBody implements Iterator<MultipartPart> {
    private final FileItemIterator iterator;

    static MultipartIteratorBody multipartIteratorBody(final FileItemIterator fileItemIterator) {
        return new MultipartIteratorBody(fileItemIterator);
    }

    @Override
    public boolean hasNext() {
        try {
            return iterator.hasNext();
        } catch (final FileUploadException | IOException e) {
            throw multipartException(e);
        }
    }

    public MultipartPart next(final String expectedControlName) {
        final MultipartPart next = next();
        final String nextControlName = next.getControlName();
        if(!nextControlName.equals(expectedControlName)) {
            throw MultipartException.multipartException("Expected next multipart part to have the control " +
                    "name '" + expectedControlName + "' but it was '" + nextControlName + "'");
        }
        return next;
    }

    @Override
    public MultipartPart next() {
        try {
            final FileItemStream item = iterator.next();
            final String controlName = item.getFieldName();
            final InputStream inputStream = item.openStream();
            if (!item.isFormField()) {
                final String name = item.getName();
                return multipartFile(controlName, name, inputStream);
            } else {
                return multipartFormControl(controlName, inputStream);
            }
        } catch (final FileUploadException | IOException e) {
            throw multipartException(e);
        }
    }
}
