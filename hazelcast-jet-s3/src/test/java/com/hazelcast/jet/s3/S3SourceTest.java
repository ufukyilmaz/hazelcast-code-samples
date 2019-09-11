/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.s3;

import com.hazelcast.jet.function.SupplierEx;
import com.hazelcast.test.annotation.NightlyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Category(NightlyTest.class)
public class S3SourceTest extends S3TestBase {

    private static final String bucketName = "jet-s3-connector-test-bucket-source";

    @Test
    public void test() {
        testSource(jet, bucketName, null, 1100, 1000);
    }

    SupplierEx<S3Client> clientSupplier() {
        return () -> S3Client.builder()
                             .region(Region.US_EAST_1)
                             .build();
    }

}
