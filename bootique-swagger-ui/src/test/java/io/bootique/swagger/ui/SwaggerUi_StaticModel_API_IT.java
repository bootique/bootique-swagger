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

import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.test.junit5.BQTestClassFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Mix of APIs, static models and Swagger UI")
public class SwaggerUi_StaticModel_API_IT {

    @RegisterExtension
    public static BQTestClassFactory testFactory = new BQTestClassFactory();

    private static WebTarget target = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @BeforeAll
    public static void startServer() {

        // server layout:
        // "/"          - jersey
        // "/doc"       - static servlet
        // "/doc/*.yml" - open API specs

        testFactory.app("-s", "-c", "classpath:SwaggerUi_StaticModel_API_IT/startup.yml")
                .autoLoadModules()
                .module(b -> JerseyModule.extend(b).addResource(Api1.class).addResource(Api2.class))
                .module(b -> JettyModule.extend(b).addStaticServlet("doc", "/doc/*"))
                .run();
    }

    @Test
    @DisplayName("APIs available")
    public void testApisAvailable() {
        Response r1 = target.path("api1").request().get();
        assertEquals(200, r1.getStatus());
        assertEquals("I am API1", r1.readEntity(String.class));

        Response r2 = target.path("api2").request().get();
        assertEquals(200, r2.getStatus());
        assertEquals("I am API2", r2.readEntity(String.class));
    }

    @Test
    @DisplayName("Static models available")
    public void testStaticModelsAvailable() {
        Response r1 = target.path("doc/api1/model.yml").request().get();
        assertEquals(200, r1.getStatus());

        SwaggerUiBaseIT.assertEqualsToResourceContents(
                "SwaggerUi_StaticModel_API_IT/doc/api1/model.yml",
                r1.readEntity(String.class));
    }

    @Path("api1")
    public static class Api1 {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Response get() {
            return Response.ok("I am API1").build();
        }
    }

    @Path("api2")
    public static class Api2 {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Response get() {
            return Response.ok("I am API2").build();
        }
    }
}
