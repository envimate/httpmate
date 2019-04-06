/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

package com.envimate.httpmate.tests.givenwhenthen.deploy.awslambda;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.tests.givenwhenthen.client.ClientFactory;
import com.envimate.httpmate.tests.givenwhenthen.client.real.RealHttpMateClientWithConnectionReuseFactory;
import com.envimate.httpmate.tests.givenwhenthen.deploy.Deployer;
import com.envimate.httpmate.tests.givenwhenthen.deploy.Deployment;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.envimate.httpmate.tests.givenwhenthen.client.real.RealHttpMateClientFactory.theRealHttpMateClient;
import static com.envimate.httpmate.tests.givenwhenthen.client.real.RealHttpMateClientWithConnectionReuseFactory.theRealHttpMateClientWithConnectionReuse;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.Deployment.httpsDeploymentWithBasePath;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.awslambda.Poller.sleep;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.awslambda.Poller.waitFor;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.awslambda.S3Handler.deleteFromS3Bucket;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.awslambda.S3Handler.uploadToS3Bucket;
import static com.envimate.httpmate.tests.givenwhenthen.deploy.awslambda.lambdastatus.LambdaStatus.isReady;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;

public final class AwsDeployer implements Deployer {

    private static final String BUCKET = "com.envimate.webmate.lambda.artifacts";
    private static final String KEY_TEMPLATE = "%s/aws-lambda-testfunction.jar";
    private static final String GATEWAY_ID = "5o3n1virag";

    private String uuid;

    private AwsDeployer() {
    }

    public static Deployer awsDeployer() {
        return new AwsDeployer();
    }

    @Override
    public Deployment ensureTheTestHttpMateInstanceIsDeployed() {
        if (uuid == null) {
            System.out.println("ensureTheTestHttpMateInstanceIsDeployed initializing");
            uuid = uuid();
            System.out.println("ensureTheTestHttpMateInstanceIsDeployed got uuid");
            final File lambdaFile = lambdaFile();
            System.out.println("ensureTheTestHttpMateInstanceIsDeployed got lamda file" + lambdaFile);
            String s3Key = s3Key();
            uploadToS3Bucket(BUCKET, s3Key, lambdaFile);
            waitFor(() -> isReady(uuid), 10, 60);
            sleep(10);
        } else {
            System.out.println("skipping, ensureTheTestHttpMateInstanceIsDeployed already initialized...");
        }
        return httpsDeploymentWithBasePath(GATEWAY_ID + ".execute-api.eu-central-1.amazonaws.com", 443, "/" + uuid);
    }

    @Override
    public Deployment deploy(final HttpMate httpMate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanUp() {
        if (uuid != null) {
            deleteFromS3Bucket(BUCKET, s3Key());
        }
    }

    private String s3Key() {
        return format(KEY_TEMPLATE, uuid);
    }

    private static File lambdaFile() {
        System.out.println("lambdaFile entered");
        final String currentDirectory = System.getProperty("user.dir");
        System.out.println("lambdaFile currentDirectory: " + currentDirectory);
        final String targetDirectory;
        if (currentDirectory.equals("/opt/atlassian/pipelines/agent/build/tests/testsuite")) {
            targetDirectory = "/opt/atlassian/pipelines/agent/build/tests/aws-lambda-testfunction/target/";
            System.out.println("lambdaFile targetDirectory: " + targetDirectory);
        } else {
            final int index = currentDirectory.indexOf("httpmate");
            System.out.println("lambdaFile index: " + index);
            targetDirectory = currentDirectory.substring(0, index) + "httpmate/tests/aws-lambda-testfunction/target/";
            System.out.println("lambdaFile targetDirectory: " + targetDirectory);
        }
        final File directory = new File(targetDirectory);
        if (!directory.exists()) {
            throw lambdaNotFoundException(targetDirectory);
        }
        final File[] listOfFiles = directory.listFiles();
        if (listOfFiles == null) {
            throw lambdaNotFoundException(targetDirectory);
        }
        final File awsLambdaJar = stream(listOfFiles)
                .filter(File::isFile)
                .filter(file -> file.getName().matches("aws-lambda-testfunction-[0-9*].[0-9*].[0-9*].jar"))
                .findAny()
                .orElseThrow(() -> lambdaNotFoundException(targetDirectory));
        System.out.println(String.format(
                "Located aws lambda jar '%s;",
                awsLambdaJar.getAbsolutePath())
        );
        return awsLambdaJar;
    }

    private static RuntimeException lambdaNotFoundException(final String expectedLamdaBuildDirectory) {
        final String message = String.format(
                "Could not find lambda jar file under '%s'. " +
                        "Please run 'mvn clean package -DskipTests' before running tests in the IDE.",
                expectedLamdaBuildDirectory);
        System.err.println(message);
        System.out.println(message);
        return new RuntimeException(message);
    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "awslambda";
    }

    @Override
    public List<ClientFactory> supportedClients() {
        return Arrays.asList(theRealHttpMateClient(), theRealHttpMateClientWithConnectionReuse());
    }
}
