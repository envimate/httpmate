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

import com.envimate.httpmate.tests.givenwhenthen.DeployerAndClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static com.envimate.httpmate.tests.givenwhenthen.Given.given;
import static com.envimate.httpmate.tests.givenwhenthen.MultipartBuilder.startingWith;
import static com.envimate.httpmate.tests.givenwhenthen.builders.MultipartElement.aFile;
import static com.envimate.httpmate.tests.givenwhenthen.builders.MultipartElement.aFormControl;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.DeployerManager.setCurrentDeployerAndClient;
import static com.envimate.httpmate.tests.multipart.MultipartHttpMateConfiguration.theMultipartHttpMateInstanceUsedForTesting;

@RunWith(Parameterized.class)
public final class MultipartSpecs {

    public MultipartSpecs(final DeployerAndClient deployerAndClient) {
        setCurrentDeployerAndClient(deployerAndClient);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DeployerAndClient> deployers() {
        return activeDeployers();
    }

    @Test
    public void testMultipartFileUploadWithGet() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaTheGetMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartFileUploadWithPost() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartFileUploadWithPut() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePutMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartFileUploadWithDelete() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaTheDeleteMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartWithoutFile() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFormControl("foo", "bar"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=foo,content=bar}]");
    }

    @Test
    public void testMultipartWithFieldsBeforeTheFileUpload() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFormControl("control1", "content1"))
                        .followedBy(aFormControl("control2", "content2"))
                        .followedBy(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=control1,content=content1}, {controlname=control2,content=content2}, {controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @Test
    public void testMultipartFieldsAfterTheFileUpload() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooooooo"))
                        .followedBy(aFormControl("ignoredcontrol1", "ignoredcontent1"))
                        .followedBy(aFormControl("ignoredcontrol2", "ignoredcontent2"))
                        .followedBy(aFile("myfile", "ignoredfile.txt", "ignoredfilecontent"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooooooo}, {controlname=ignoredcontrol1,content=ignoredcontent1}, {controlname=ignoredcontrol2,content=ignoredcontent2}, {controlname=myfile,filename=ignoredfile.txt,content=ignoredfilecontent}]");
    }

    @Test
    public void testMultipartWithMultipleFiles() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFile("file1", "file1.pdf", "content1"))
                        .followedBy(aFile("file2", "file2.pdf", "content2"))
                        .followedBy(aFile("file3", "file3.pdf", "content3"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=file1,filename=file1.pdf,content=content1}, {controlname=file2,filename=file2.pdf,content=content2}, {controlname=file3,filename=file3.pdf,content=content3}]");
    }

    @Test
    public void testAuthenticationByFirstMultipartPart() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/authenticated").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFormControl("authentication", "username=bob"))
                        .followedBy(aFormControl("control1", "foo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("Authenticated as: bob");
    }

    @Test
    public void testWrongAuthorizationGetsRejectedBeforeProcessingFile() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/authorized").viaThePostMethod().withTheMultipartBody(startingWith(aFormControl("authentication", "username=normaluser"))
                .followedBy(aFile("file", "file.txt", "content"))).isIssued()
                .theStatusCodeWas(403)
                .theResponseBodyWas("Access denied!");
    }

    @Test
    public void testAuthorizationBeforeProcessingFile() {
        given(theMultipartHttpMateInstanceUsedForTesting())
                .when().aRequestToThePath("/authorized").viaThePostMethod().withTheMultipartBody(startingWith(aFormControl("authentication", "username=admin"))
                .followedBy(aFile("file", "file.txt", "content"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("Welcome to the admin section!");
    }
}
