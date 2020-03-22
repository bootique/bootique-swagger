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

import io.bootique.jersey.JerseyModule;
import io.bootique.test.junit.BQTestFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.Assert.*;

public class SwaggerOpenapiModuleIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    private static WebTarget BASE_TARGET = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @BeforeClass
    public static void beforeClass() {
        TEST_FACTORY.app("-s")
                .module(new SwaggerOpenapiModuleProvider())
                .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
                .run();
    }

    @Test
    public void testApi_Yaml() {

        Response r = BASE_TARGET.path("/openapi.yaml").request().get();
        assertEquals(200, r.getStatus());
        assertEqualsToResourceContents(r.readEntity(String.class), "response1.yml", "response1_alt.yml");
    }

    @Test
    public void testApi_Json() {

        Response r = BASE_TARGET.path("/openapi.json").request().get();
        assertEquals(200, r.getStatus());
        assertEqualsToResourceContents(r.readEntity(String.class), "response1.json", "response1_alt.json");
    }

    // allow comparison with multiple alternatives, as reflection method order seems to be JVM dependent
    private void assertEqualsToResourceContents(String toTest, String... expectedAlternatives) {

        ClassLoader cl = getClass().getClassLoader();

        for (int i = 0; i < expectedAlternatives.length; i++) {
            String expected = expectedAlternatives[i];


            try (InputStream in = cl.getResourceAsStream(expected)) {
                assertNotNull(in);

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

    @OpenAPIDefinition
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
