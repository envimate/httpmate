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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;

import static com.amazonaws.services.s3.AmazonS3ClientBuilder.defaultClient;

final class S3Handler {

    private S3Handler() {
    }

    static void uploadToS3Bucket(final String bucketName,
                                 final String key,
                                 final File file) {
        final AmazonS3 amazonS3 = defaultClient();
        final PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
        amazonS3.putObject(request);
    }

    static void deleteFromS3Bucket(final String bucketName,
                                   final String key) {
        final AmazonS3 amazonS3 = defaultClient();
        final DeleteObjectRequest request = new DeleteObjectRequest(bucketName, key);
        amazonS3.deleteObject(request);
    }
}
