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
import io.bootique.junit5.BQTestFactory;
import io.bootique.resource.ResourceFactory;
import io.bootique.swagger.config3.Api31;
import io.bootique.swagger.config3a.Api3a;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationSpecsIT {

    private static final WebTarget target = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @RegisterExtension
    public final BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    @Test
    @DisplayName("API classes can be picked individually")
    public void testResourceClassesFilter() {
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
    public void testResourcePackagesFilter() {
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
