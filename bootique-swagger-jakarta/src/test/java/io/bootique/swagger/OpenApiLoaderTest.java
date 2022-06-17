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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import jakarta.ws.rs.core.Application;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class OpenApiLoaderTest {

    @Test
    public void testSortPaths() {

        Paths paths = new Paths();
        paths.addPathItem("a/{id}/c", new PathItem());
        paths.addPathItem("a/{id}", new PathItem());
        paths.addPathItem("b", new PathItem());
        paths.addPathItem("a", new PathItem());

        OpenAPI openAPI = new OpenAPI();
        openAPI.setPaths(paths);
        OpenApiLoader loader = new OpenApiLoader(mock(Application.class));

        loader.sortPaths(openAPI);

        Paths sorted = openAPI.getPaths();
        assertEquals(new HashSet<>(asList("a", "a/{id}", "a/{id}/c", "b")), sorted.keySet());
    }
}
