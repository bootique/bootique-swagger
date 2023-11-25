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
package io.bootique.swagger.customizer;

import io.bootique.swagger.OpenApiCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;

/**
 * @since 3.0
 * @deprecated in favor of the Jakarta flavor
 */
@Deprecated(since = "3.0", forRemoval = true)
public class PathSortingCustomizer implements OpenApiCustomizer {

    @Override
    public void customize(String name, OpenAPI api) {
        Paths paths = api.getPaths();
        if (paths != null && paths.keySet().size() >= 2) {
            Paths sorted = new Paths();
            paths.keySet().stream().sorted().forEach(p -> sorted.put(p, paths.get(p)));
            api.setPaths(sorted);
        }
    }
}
