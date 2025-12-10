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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jackson.JacksonService;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.swagger.config10.TestApis;
import io.swagger.v3.oas.models.PathItem;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class OpenApiRequestModelFilterIT {

    static BiPredicate<String, PathItem.HttpMethod>[] allowPathAndMethodCheck = new BiPredicate[1];

    final JettyTester jetty = JettyTester.create();

    @BQApp
    final BQRuntime app = Bootique
            .app("-s", "-c", "classpath:config10/startup.yml")
            .autoLoadModules()
            .module(jetty.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(TestApis.class))
            .module(b -> SwaggerModule.extend(b).addRequestCustomizer(new OpenApiRequestModelFilter((p, m) -> allowPathAndMethodCheck[0].test(p, m))))
            .createRuntime();


    @ParameterizedTest
    @MethodSource
    public void includeExcludePaths(List<String> allowedPaths, Map<String, Boolean> expectedPathsPolicy) throws JsonProcessingException {
        allowPathAndMethodCheck[0] = (p, m) -> allowedPaths.contains(p);

        Response r = jetty.getTarget().path("/model.json")
                .request()
                .get();
        String modelJson = JettyTester.assertOk(r).getContentAsString();
        JsonNode pathsNode = app.getInstance(JacksonService.class).newObjectMapper().readTree(modelJson).get("paths");

        Set<String> paths = new HashSet<>();
        pathsNode.fieldNames().forEachRemaining(paths::add);

        for (Map.Entry<String, Boolean> e : expectedPathsPolicy.entrySet()) {
            if (e.getValue()) {
                assertEquals(e.getValue(), paths.contains(e.getKey()), () -> "Path '" + e.getKey() + "' should not be allowed for " + allowedPaths);
            }
        }
    }

    static Stream<Arguments> includeExcludePaths() {
        return Stream.of(
                Arguments.arguments(List.of("/t/hi", "/t/1", "/t/2"), Map.of("/t/hi", true, "/t/1", true, "/t/2", true)),
                Arguments.arguments(List.of("/t/hi"), Map.of("/t/hi", true, "/t/1", false, "/t/2", false)),
                Arguments.arguments(List.of("/t/2"), Map.of("/t/hi", false, "/t/1", false, "/t/2", true))
        );
    }


    @Test
    public void includeExcludeMethods() throws JsonProcessingException {
        allowPathAndMethodCheck[0] = (p, m) -> p.equals("/t/hi") || (p.equals("/t/1") && m == PathItem.HttpMethod.PUT);

        Response r = jetty.getTarget().path("/model.json")
                .request()
                .get();
        String modelJson = JettyTester.assertOk(r).getContentAsString();
        JsonNode pathsNode = app.getInstance(JacksonService.class).newObjectMapper().readTree(modelJson).get("paths");
        assertEquals(2, pathsNode.size());

        JsonNode tHi = pathsNode.get("/t/hi");
        assertNotNull(tHi.get("get"));

        JsonNode t1 = pathsNode.get("/t/1");
        assertNull(t1.get("get"));
        assertNotNull(t1.get("put"));

        assertNull(pathsNode.get("/t/2"));
    }

    @ParameterizedTest
    @MethodSource
    public void includeExcludeSchemas(List<String> allowedPaths, Set<String> expectedSchemas) throws JsonProcessingException {
        allowPathAndMethodCheck[0] = (p, m) -> allowedPaths.contains(p);

        Response r = jetty.getTarget().path("/model.json")
                .request()
                .get();
        String modelJson = JettyTester.assertOk(r).getContentAsString();
        JsonNode componentsNode = app.getInstance(JacksonService.class).newObjectMapper().readTree(modelJson).get("components");

        Set<String> schemas = new HashSet<>();
        componentsNode.get("schemas").fieldNames().forEachRemaining(s -> {
            // only bother with test schemas
            if (s.startsWith("TestO")) {
                schemas.add(s);
            }
        });

        assertEquals(expectedSchemas, schemas);
    }

    static Stream<Arguments> includeExcludeSchemas() {
        return Stream.of(
                Arguments.arguments(List.of("/t/hi", "/t/1", "/t/2", "/t/3", "/t/4"), Set.of("TestO1", "TestO2", "TestO3", "TestO4")),
                Arguments.arguments(List.of("/t/hi"), Set.of()),
                Arguments.arguments(List.of("/t/2"), Set.of("TestO1", "TestO2")),
                Arguments.arguments(List.of("/t/3"), Set.of("TestO1", "TestO3")),
                Arguments.arguments(List.of("/t/4"), Set.of("TestO1", "TestO3", "TestO4"))
        );
    }

}
