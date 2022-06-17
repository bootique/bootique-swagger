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
package io.bootique.swagger.jakarta.ui;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.resource.ResourceFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Multi-tenancy: mix of APIs, OpenAPI models and swagger-ui consoles")
@BQTest
public class MultiTenant_StaticModel_API_IT {

    static final JettyTester jetty = JettyTester.create();

    // URL layout:

    // "/"                          - Jersey root
    // "/api*"                      - API endpoints handled by Jersey

    // "/models"                    - Servlet serving OpenAPI specs
    // "/models/api*./model.yml"    - OpenAPI specs

    // "/doc/"                      - documentation root
    // "/doc/api*"                  - swagger-ui consoles

    @BQApp
    static final BQRuntime app = Bootique
            .app("-s", "-c", "classpath:MultiTenant_StaticModel_API_IT/startup.yml")
            .autoLoadModules()
            .module(jetty.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(Api1.class).addResource(Api2.class))
            .module(b -> JettyModule.extend(b).addStaticServlet("models", "/models/*"))
            .module(b -> JettyModule.extend(b).addStaticServlet("doc", "/doc/*"))
            .createRuntime();


    @Test
    @DisplayName("APIs available")
    public void testApisAvailable() {
        Response r1 = jetty.getTarget().path("api1").request().get();
        JettyTester.assertOk(r1).assertContent("I am API1");

        Response r2 = jetty.getTarget().path("api2").request().get();
        JettyTester.assertOk(r2).assertContent("I am API2");
    }

    @Test
    @DisplayName("Static models available")
    public void testStaticModelsAvailable() {
        Response r1 = jetty.getTarget().path("models/api1/model.yml").request().get();
        JettyTester.assertOk(r1)
                .assertContent(new ResourceFactory("classpath:MultiTenant_StaticModel_API_IT/models/api1/model.yml"));

        Response r2 = jetty.getTarget().path("models/api2/model.yml").request().get();
        JettyTester.assertOk(r2)
                .assertContent(new ResourceFactory("classpath:MultiTenant_StaticModel_API_IT/models/api2/model.yml"));
    }

    @Test
    @DisplayName("Static docs available")
    public void testStaticDocsAvailable() {
        Response r = jetty.getTarget().path("doc").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:MultiTenant_StaticModel_API_IT/doc/index.html"));
    }

    @Test
    @DisplayName("Swagger UI")
    public void testMultipleSwaggerUIAvailable() {

        String baseUrl = jetty.getUrl();

        Response r1 = jetty.getTarget().path("doc/api1").request().get();
        JettyTester.assertOk(r1)
                .assertContent(s -> s.contains("url: \"" + baseUrl + "/models/api1/model.yml\""));

        Response r2 = jetty.getTarget().path("doc/api2").request().get();
        JettyTester.assertOk(r2)
                .assertContent(s -> s.contains("url: \"" + baseUrl + "/models/api2/model.yml\""));
    }

    @Test
    @DisplayName("Swagger UI Resources")
    public void testMultipleSwaggerUIResourcesAvailable() {
        Response r1 = jetty.getTarget().path("doc/api1/static/swagger-ui.css").request().get();
        JettyTester.assertOk(r1);

        Response r2 = jetty.getTarget().path("doc/api2/static/swagger-ui.css").request().get();
        JettyTester.assertOk(r2);
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
