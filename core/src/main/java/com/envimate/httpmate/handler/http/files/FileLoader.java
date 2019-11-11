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

package com.envimate.httpmate.handler.http.files;

import com.envimate.httpmate.util.Validators;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.envimate.httpmate.handler.http.files.FileDoesNotExistException.filesystemFileDoesNotExistException;
import static com.envimate.httpmate.handler.http.files.FileDoesNotExistException.javaResourceDoesNotExistException;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class FileLoader {

    private FileLoader() {
    }

    public static InputStream loadFile(final File file) {
        validateNotNull(file, "file");
        try {
            return new FileInputStream(file);
        } catch (final IOException e) {
            throw filesystemFileDoesNotExistException(file.getAbsolutePath(), e);
        }
    }

    public static InputStream loadJavaResource(final MultiformatPath path) {
        final ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        final String properlyFormatted = path.formatted("", "");
        final URL resource = contextClassLoader.getResource(properlyFormatted);
        if (isNull(resource) || isDirectory(contextClassLoader, path, resource)) {
            throw javaResourceDoesNotExistException(path);
        }
        try {
            return resource.openStream();
        } catch (final IOException e) {
            throw javaResourceDoesNotExistException(path, e);
        }
    }

    private static boolean isDirectory(final ClassLoader classLoader,
                                       final MultiformatPath path,
                                       final URL properlyLoadedResource) {
        Validators.validateNotNull(properlyLoadedResource, "properlyLoadedResource");
        final String protocol = properlyLoadedResource.getProtocol();
        if ("file".equals(protocol)) {
            final File file = new File(properlyLoadedResource.getPath());
            return file.isDirectory();
        } else if ("jar".equals(protocol)) {
            final String asDirectoryPath = path.formatted("", "/");
            final URL asDirectoryLoadedResources = classLoader.getResource(asDirectoryPath);
            return nonNull(asDirectoryLoadedResources);
        } else {
            throw new UnsupportedOperationException(format(
                    "Not able to load resource '%s' because protocol '%s' is not supported", path, protocol));
        }
    }
}
