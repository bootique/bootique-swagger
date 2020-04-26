/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
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
package io.bootique.swagger.openapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class OpenApiAsserts {

    // allow comparison with multiple alternatives, as reflection method order seems to be JVM dependent
    // TODO: this is really a bug in Swagger. Maybe version > 2.0.6 can deal with it better. Also Swagger 2 spec
    //  is not prone to this issue, even though it does the same reflection. Are they reordering explicitly?
    static void assertEqualsToResource(String toTest, String... expectedAlternatives) {

        ClassLoader cl = OpenApiAsserts.class.getClassLoader();

        for (int i = 0; i < expectedAlternatives.length; i++) {
            String expected = expectedAlternatives[i];

            try (InputStream in = cl.getResourceAsStream(expected)) {
                assertNotNull("Expected resource " + expected + " not found", in);

                // read as bytes to preserve line breaks
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = in.read(data, 0, data.length)) != -1) {
                    out.write(data, 0, nRead);
                }

                String expectedString = new String(out.toByteArray(), "UTF-8");

                // don't use assert if there are still alternatives left
                if (i < expectedAlternatives.length - 1) {
                    if (Objects.equals(expectedString, toTest)) {
                        break;
                    }
                } else {
                    assertEquals(expectedString, toTest);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
