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

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.resource.ResourceFactory;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

@BQTest
public class RequestCustomizerIT {

    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("-s", "-c", "classpath:config8/startup.yml")
            .autoLoadModules()
            .module(jetty.moduleReplacingConnectors())
            .module(b -> SwaggerModule.extend(b).addRequestCustomizer(RequestCustomizerIT::customizeResponse))
            .createRuntime();

    @Test
    public void customModel() {
        Response r = jetty.getTarget().path("/model.json").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:config8/response.json"));
    }

    private static void customizeResponse(HttpServletRequest r, Supplier<OpenAPI> s) {

        // check OpenAPI scope
        OpenAPI shared = app.getInstance(SwaggerService.class).getOpenApiModel("model.json").getApi();
        OpenAPI api1 = s.get();
        OpenAPI api2 = s.get();

        assertNotSame(shared, api1);
        assertSame(api1, api2);

        // filter
        api1.getPaths().remove("/api7/7");
        api1.getComponents().getSchemas().remove("OB");
    }
}
