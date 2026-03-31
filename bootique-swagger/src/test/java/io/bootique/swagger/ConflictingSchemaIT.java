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
import io.bootique.junit.BQApp;
import io.bootique.junit.BQTest;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BQTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConflictingSchemaIT {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app("-s", "-c", "classpath:config11/startup.yml")
            .autoLoadModules()
            .createRuntime();

    @BQApp(skipRun = true)
    static final BQRuntime app12 = Bootique
            .app("-s", "-c", "classpath:config12/startup.yml")
            .autoLoadModules()
            .createRuntime();

    @BQApp(skipRun = true)
    static final BQRuntime app13 = Bootique
            .app("-s", "-c", "classpath:config13/startup.yml")
            .autoLoadModules()
            .createRuntime();

    @Test
    // putting this test in front to ensure we can capture stderr before pollution by other tests
    @Order(1)
    public void conflictingSchemaWarn() {
        PrintStream savedErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured));

        try {
            app.getInstance(SwaggerService.class).getOpenApiModel("model.json").getApi();
        } finally {
            System.setErr(savedErr);
        }

        String log = captured.toString();
        assertTrue(log.contains("** Schema name 'MySchema' is already used by"), log);
        assertTrue(log.contains("io.bootique.swagger.config11.p1.MySchema"), "Expected p1.MySchema in warning");
        assertTrue(log.contains("io.bootique.swagger.config11.p2.MySchema"), "Expected p2.MySchema in warning");
    }

    @Test
    @Order(2)
    public void conflictingSchemaRenamed() {
        SwaggerService service = app.getInstance(SwaggerService.class);
        OpenAPI api = service.getOpenApiModel("model.json").getApi();
        Map<String, ?> schemas = api.getComponents().getSchemas();
        assertEquals(2, schemas.size(), schemas.keySet().toString());
        assertTrue(schemas.containsKey("MySchema"), "Original schema 'MySchema' must be present");
        assertTrue(schemas.containsKey("MySchema1"), "Renamed schema 'MySchema1' must be present");
    }

    @Test
    public void threeWayConflict() {
        SwaggerService service = app12.getInstance(SwaggerService.class);
        OpenAPI api = service.getOpenApiModel("model.json").getApi();
        Map<String, ?> schemas = api.getComponents().getSchemas();
        assertEquals(3, schemas.size(), schemas.keySet().toString());
        assertTrue(schemas.containsKey("MySchema"), "Original schema 'MySchema' must be present");
        assertTrue(schemas.containsKey("MySchema1"), "Renamed schema 'MySchema1' must be present");
        assertTrue(schemas.containsKey("MySchema2"), "Renamed schema 'MySchema2' must be present");
    }

    @Test
    public void subschemaConflict() {
        SwaggerService service = app13.getInstance(SwaggerService.class);
        OpenAPI api = service.getOpenApiModel("model.json").getApi();
        Map<String, ?> schemas = api.getComponents().getSchemas();
        // Container and Box are top-level (no conflict), Inner conflicts and gets renamed
        assertEquals(4, schemas.size(), schemas.keySet().toString());
        assertTrue(schemas.containsKey("Container"), "'Container' must be present");
        assertTrue(schemas.containsKey("Box"), "'Box' must be present");
        assertTrue(schemas.containsKey("Inner"), "Original 'Inner' must be present");
        assertTrue(schemas.containsKey("Inner1"), "Renamed 'Inner1' must be present");
    }
}
