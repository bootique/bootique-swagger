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
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @since 3.0
 */
public class SchemasSortingCustomizer implements OpenApiCustomizer {

    @Override
    public void customize(String name, Supplier<OpenAPI> apiSupplier) {
        OpenAPI api = apiSupplier.get();
        Components components = api.getComponents();
        if (components != null) {

            Map<String, Schema> schemas = components.getSchemas();
            if (schemas != null && schemas.size() >= 2) {
                LinkedHashMap<String, Schema> sorted = new LinkedHashMap<>();
                schemas.keySet().stream().sorted().forEach(s -> sorted.put(s, schemas.get(s)));
                components.setSchemas(sorted);
            }
        }
    }
}
