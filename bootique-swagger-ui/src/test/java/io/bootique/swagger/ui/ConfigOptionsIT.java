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

import io.bootique.swagger.SwaggerAsserts;
import io.bootique.test.junit5.BQTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("'swaggerui' config options")
public class ConfigOptionsIT {

    private static final WebTarget target = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @RegisterExtension
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    @DisplayName("'specPath' in config")
    public void testSpecPath() {
        testFactory.app("-s", "-c", "classpath:ConfigOptionsIT/specpath.yml").autoLoadModules().run();

        Response r = target.path("/swagger-ui").request().get();
        assertEquals(200, r.getStatus());
        SwaggerAsserts.assertEqualsToResource("ConfigOptionsIT/response1.html", r.readEntity(String.class));
    }

    @Test
    @DisplayName("'specUrl' in config")
    public void testSpecUrl() {
        testFactory.app("-s", "-c", "classpath:ConfigOptionsIT/specurl.yml").autoLoadModules().run();

        Response r = target.path("/swagger-ui").request().get();
        assertEquals(200, r.getStatus());
        SwaggerAsserts.assertEqualsToResource("ConfigOptionsIT/response2.html", r.readEntity(String.class));
    }

    @Test
    @DisplayName("'uiPath' in config")
    public void testApi_Console() {
        testFactory.app("-s", "-c", "classpath:ConfigOptionsIT/uipath.yml").autoLoadModules().run();

        Response r = target.path("/swagger-uix").request().get();
        assertEquals(200, r.getStatus());
        SwaggerAsserts.assertEqualsToResource("ConfigOptionsIT/response3.html", r.readEntity(String.class));
    }
}