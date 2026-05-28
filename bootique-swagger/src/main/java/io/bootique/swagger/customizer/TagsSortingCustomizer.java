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
import io.swagger.v3.oas.models.tags.Tag;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @since 3.0
 */
public class TagsSortingCustomizer implements OpenApiCustomizer {

    @Override
    public void customize(String name, Supplier<OpenAPI> apiSupplier) {
        OpenAPI api = apiSupplier.get();

        // Collect all tags, preserving any top-level Tag objects that carry descriptions / externalDocs,
        // and adding placeholders for tag names referenced only in operations
        Map<String, Tag> tags = new HashMap<>();
        if (api.getTags() != null) {
            api.getTags().forEach(t -> tags.put(t.getName(), t));
        }

        if (api.getPaths() != null) {
            api.getPaths().values().stream()
                    .flatMap(pathItem -> pathItem.readOperations().stream())
                    .filter(op -> op.getTags() != null)
                    .flatMap(op -> op.getTags().stream())
                    .forEach(n -> tags.computeIfAbsent(n, k -> new Tag().name(k)));
        }

        if (!tags.isEmpty()) {
            List<Tag> sorted = tags.values().stream()
                    .sorted(Comparator.comparing(Tag::getName))
                    .collect(Collectors.toList());
            api.setTags(sorted);
        }
    }
}
