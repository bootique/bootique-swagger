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
package io.bootique.swagger;

import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.resource.ResourceFactory;
import io.bootique.swagger.config3.Api31;
import io.bootique.swagger.config3a.Api3a;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@BQTest
public class AnnotationSpecsIT {

    private static final WebTarget target = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    @Test
    @DisplayName("No web access flag should result in 404")
    public void noWebAccess() {
        testFactory.app("-s", "-c", "classpath:config3/startup0.yml")
                .module(b -> JerseyModule.extend(b).addPackage(Api31.class))
                .run();

        Response r = target.path("/c3/model.yaml").request().get();
        JettyTester.assertNotFound(r)
                .assertContentType(MediaType.TEXT_HTML_TYPE)
                .assertContent(c -> c.contains("<h2>HTTP ERROR 404 Not Found</h2>"));
    }

    @Test
    @DisplayName("API classes can be picked individually")
    public void resourceClassesFilter() {
        testFactory.app("-s", "-c", "classpath:config3/startup1.yml")
                // Contribute the entire package in runtime...
                // Make sure only classes in "resourceClass" are includes in the spec
                .module(b -> JerseyModule.extend(b).addPackage(Api31.class))
                .run();

        Response r = target.path("/c3/model.yaml").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:config3/response1.yml"));
    }

    @Test
    @DisplayName("API packages can be picked individually")
    public void resourcePackagesFilter() {
        testFactory.app("-s", "-c", "classpath:config3/startup2.yml")
                // Contribute multiple packages to runtime...
                // Make sure only classes in "resourcePackages" are includes in the spec
                .module(b -> JerseyModule.extend(b).addPackage(Api31.class).addPackage(Api3a.class))
                .run();

        Response r = target.path("/c3/model.yaml").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:config3/response2.yml"));
    }
}
