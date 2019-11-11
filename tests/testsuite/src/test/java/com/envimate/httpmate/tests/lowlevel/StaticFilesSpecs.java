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

package com.envimate.httpmate.tests.lowlevel;

import com.envimate.httpmate.handler.http.files.FileDoesNotExistException;
import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.http.impl.auth.UnsupportedDigestAlgorithmException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@RunWith(Parameterized.class)
public final class StaticFilesSpecs {
    private static final String BASE_PATH = locateBasePath();

    private static final List<FileDescriptor> CORRECT_FILES = Stream.of("/file1", "/file2", "/file3", "/subdirectory/file4", "/subdirectory/file5")
            .map(FileDescriptor::file)
            .collect(toList());

    private static final List<String> INCORRECT_FILES = Arrays.asList("/not_a_file", "/subdirectory", "~", "~root");

    public StaticFilesSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Test
    public void aFileCanBeServed() {
        given(
                anHttpMate()
                        .get("/file", (request, response) -> response.setFileAsBody(BASE_PATH + "/file1"))
                        .build()
        )
                .when().aRequestToThePath("/file").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("this is file1");
    }

    @Test
    public void aNonExistentFileLeadsToAnException() {
        given(
                anHttpMate()
                        .get("/file", (request, response) -> response.setFileAsBody(BASE_PATH + "/not_a_file"))
                        .configured(toMapExceptionsOfType(FileDoesNotExistException.class, (exception, response) -> response.setStatus(404)))
                        .build()
        )
                .when().aRequestToThePath("/file").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(404)
                .theResponseBodyWas("");
    }

    @Test
    public void aDirectoryLeadsToAnException() {
        given(
                anHttpMate()
                        .get("/file", (request, response) -> response.setFileAsBody(BASE_PATH + "/directory"))
                        .configured(toMapExceptionsOfType(FileDoesNotExistException.class, (exception, response) -> response.setStatus(404)))
                        .build()
        )
                .when().aRequestToThePath("/file").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(404)
                .theResponseBodyWas("");
    }

    @Test
    public void aJavaResourceCanBeServed() {
        given(
                anHttpMate()
                        .get("/resource", (request, response) -> response.setJavaResourceAsBody("staticfiles/directory/file1"))
                        .build()
        )
                .when().aRequestToThePath("/resource").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("this is file1");
    }

    @Test
    public void aNonExistentJavaResourceLeadsToAnException() {
        given(
                anHttpMate()
                        .get("/resource", (request, response) -> response.setJavaResourceAsBody("staticfiles/directory/not_a_file"))
                        .configured(toMapExceptionsOfType(FileDoesNotExistException.class, (exception, response) -> response.setStatus(404)))
                        .build()
        )
                .when().aRequestToThePath("/resource").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(404)
                .theResponseBodyWas("");
    }

    @Test
    public void aJavaResourceDirectoryLeadsToAnException() {
        given(
                anHttpMate()
                        .get("/resource", (request, response) -> response.setJavaResourceAsBody("staticfiles/directory"))
                        .configured(toMapExceptionsOfType(FileDoesNotExistException.class, (exception, response) -> response.setStatus(404)))
                        .build()
        )
                .when().aRequestToThePath("/resource").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(404)
                .theResponseBodyWas("");
    }

    @Test
    public void aJavaResourceDirectoryInAJarFileLeadsToAnException() {
        given(
                anHttpMate()
                        .get("/resource", (request, response) -> response.setJavaResourceAsBody("com/envimate/messageMate/channel"))
                        .configured(toMapExceptionsOfType(FileDoesNotExistException.class, (exception, response) -> response.setStatus(404)))
                        .build()
        )
                .when().aRequestToThePath("/resource").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(404)
                .theResponseBodyWas("");
    }

    @Test
    public void aFileSystemDirectoryCanBeServed() {
        for (final FileDescriptor file : CORRECT_FILES) {
            given(
                    anHttpMate()
                            .get("*", (request, response) -> response.mapPathToFileInDirectory(BASE_PATH))
                            .build()
            )
                    .when().aRequestToThePath(file.path).viaTheGetMethod().withAnEmptyBody().isIssued()
                    .theStatusCodeWas(200)
                    .theResponseBodyWas(file.content);
        }
    }

    @Test
    public void aFileSystemDirectoryCanBeServedWithAPrefix() {
        for (final FileDescriptor file : CORRECT_FILES) {
            given(
                    anHttpMate()
                            .get("/static/*", (request, response) -> response.mapPathToFileInDirectory(BASE_PATH, "/static/"))
                            .build()
            )
                    .when().aRequestToThePath("/static" + file.path).viaTheGetMethod().withAnEmptyBody().isIssued()
                    .theStatusCodeWas(200)
                    .theResponseBodyWas(file.content);
        }
    }

    @Test
    public void aNonExistentFileInAServedFileSystemDirectoryLeadsToAnException() {
        for (final String file : INCORRECT_FILES) {
            given(
                    anHttpMate()
                            .get("*", (request, response) -> response.mapPathToFileInDirectory(BASE_PATH))
                            .configured(toMapExceptionsOfType(FileDoesNotExistException.class, (exception, response) -> response.setStatus(404)))
                            .build()
            )
                    .when().aRequestToThePath(file).viaTheGetMethod().withAnEmptyBody().isIssued()
                    .theStatusCodeWas(404)
                    .theResponseBodyWas("");
        }
    }

    @Test
    public void aJavaResourcesDirectoryCanBeServed() {
        for (final FileDescriptor file : CORRECT_FILES) {
            given(
                    anHttpMate()
                            .get("*", (request, response) -> response.mapPathToJavaResourceInDirectory("staticfiles/directory"))
                            .build()
            )
                    .when().aRequestToThePath(file.path).viaTheGetMethod().withAnEmptyBody().isIssued()
                    .theStatusCodeWas(200)
                    .theResponseBodyWas(file.content);
        }
    }

    @Test
    public void aJavaResourcesDirectoryCanBeServedWithPrefix() {
        for (final FileDescriptor file : CORRECT_FILES) {
            given(
                    anHttpMate()
                            .get("/static/*", (request, response) -> response.mapPathToJavaResourceInDirectory("staticfiles/directory", "/static"))
                            .build()
            )
                    .when().aRequestToThePath("/static" + file.path).viaTheGetMethod().withAnEmptyBody().isIssued()
                    .theStatusCodeWas(200)
                    .theResponseBodyWas(file.content);
        }
    }

    @Test
    public void aNonExistentFileInAServedJavaResourcesDirectoryLeadsToAnException() {
        for (final String file : INCORRECT_FILES) {
            given(
                    anHttpMate()
                            .get("*", (request, response) -> response.mapPathToJavaResourceInDirectory("staticfiles/directory"))
                            .configured(toMapExceptionsOfType(FileDoesNotExistException.class, (exception, response) -> response.setStatus(404)))
                            .build()
            )
                    .when().aRequestToThePath(file).viaTheGetMethod().withAnEmptyBody().isIssued()
                    .theStatusCodeWas(404)
                    .theResponseBodyWas("");
        }
    }

    private static String locateBasePath() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL url = classLoader.getResource("staticfiles/directory");
        if (isNull(url) || !url.getProtocol().equals("file")) {
            throw new UnsupportedDigestAlgorithmException("Tests are not running with file classloader");
        }
        return url.getPath();
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class FileDescriptor {
        public final String path;
        public final String content;

        static FileDescriptor file(final String path) {
            final String filename = Paths.get(path).getFileName().toString();
            final String content = String.format("this is %s", filename);
            return new FileDescriptor(path, content);
        }
    }
}
