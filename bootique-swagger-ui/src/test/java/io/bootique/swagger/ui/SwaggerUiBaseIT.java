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

package io.bootique.swagger.ui;

import java.util.Scanner;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import io.bootique.test.junit.BQTestFactory;
import io.swagger.annotations.Api;
import org.junit.ClassRule;

import static org.junit.Assert.assertEquals;

public class SwaggerUiBaseIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    static WebTarget BASE_TARGET = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    void assertEqualsToResourceContents(String expectedResource, String toTest) {
        try (Scanner scanner = new Scanner(getClass().getClassLoader()
                .getResourceAsStream(expectedResource), "UTF-8")) {

            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine()).append(System.lineSeparator());
            }

            assertEquals(builder.toString(), toTest);
        }
    }

    @Api
    @Path("/")
    public static class TestApi {

        @GET
        public String get() {
            return "get_";
        }

        @GET
        @Path("/sub/{id}")
        public String subget(@PathParam("id") int id) {
            return "get_" + id;
        }
    }
}
