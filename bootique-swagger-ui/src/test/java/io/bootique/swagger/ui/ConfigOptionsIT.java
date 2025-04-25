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

import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.resource.ResourceFactory;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
@DisplayName("'swaggerui' config options")
public class ConfigOptionsIT {

    private static final WebTarget target = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    @Test
    @DisplayName("'noWebAccess' should be respected")
    public void noWebAccess() {
        testFactory.app("-s", "-c", "classpath:ConfigOptionsIT/nowebaccess.yml").autoLoadModules().run();

        Response r = target.path("/swagger-ui").request().get();
        JettyTester.assertNotFound(r);
    }

    @Test
    @DisplayName("no config")
    public void config() {
        // must start, but serve nothing
        testFactory.app("-s").autoLoadModules().run();

        Response r = target.path("/swagger-ui").request().get();
        assertEquals(404, r.getStatus());
    }

    @Test
    @DisplayName("no 'specPath' or 'specUrl' in config")
    public void noSpecUrlNoSpecPath() {
        // must start, but serve nothing
        testFactory.app("-s", "-c", "classpath:ConfigOptionsIT/nospecs.yml").autoLoadModules().run();

        Response r = target.path("/swagger-ui-nospecs").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:ConfigOptionsIT/response0.html"));
    }

    @Test
    @DisplayName("'specPath' in config")
    public void specPath() {
        testFactory.app("-s", "-c", "classpath:ConfigOptionsIT/specpath.yml").autoLoadModules().run();

        Response r = target.path("/swagger-ui").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:ConfigOptionsIT/response1.html"));
    }

    @Test
    @DisplayName("'specUrl' in config")
    public void specUrl() {
        testFactory.app("-s", "-c", "classpath:ConfigOptionsIT/specurl.yml").autoLoadModules().run();

        Response r = target.path("/swagger-ui").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:ConfigOptionsIT/response2.html"));
    }

    @Test
    @DisplayName("'uiPath' in config")
    public void api_Console() {
        testFactory.app("-s", "-c", "classpath:ConfigOptionsIT/uipath.yml").autoLoadModules().run();

        Response r = target.path("/swagger-uix").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:ConfigOptionsIT/response3.html"));
    }
}
