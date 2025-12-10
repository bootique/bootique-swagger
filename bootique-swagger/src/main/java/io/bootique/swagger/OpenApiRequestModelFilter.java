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
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A useful {@link OpenApiRequestCustomizer} that filters an OpenAPI model based on user-provided criteria (such as
 * security rules). Removes paths and schemas that are not accessible to the request user. It is not injectable by
 * default, and the application will need to initialize and contribute it via the extender on its own.
 *
 * @since 4.0
 */
public class OpenApiRequestModelFilter implements OpenApiRequestCustomizer {

    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    private final BiPredicate<String, PathItem.HttpMethod> allowPathAndMethodCheck;

    public OpenApiRequestModelFilter(BiPredicate<String, PathItem.HttpMethod> allowPathAndMethodCheck) {
        this.allowPathAndMethodCheck = allowPathAndMethodCheck;
    }

    @Override
    public void customize(HttpServletRequest request, Supplier<OpenAPI> apiSupplier) {
        OpenAPI api = apiSupplier.get();

        if (api.getPaths() != null) {

            // get rid of inaccessible paths
            Set<String> pathsInUse = pathsInUse(api);
            api.getPaths().keySet().removeIf(p -> !pathsInUse.contains(p));

            // get rid of unused schemas
            if (api.getComponents() != null && api.getComponents().getSchemas() != null) {
                Set<String> refsInUse = schemaRefsInUse(api);
                api.getComponents().getSchemas().keySet().removeIf(s -> !refsInUse.contains(s));
            }
        }
    }

    private Set<String> pathsInUse(OpenAPI api) {
        return api.getPaths().entrySet().stream()
                // strip off disallowed operations, and if nothing is left, remove the path
                .map(e -> filterOps(e.getKey(), e.getValue()) ? e.getKey() : null)
                .collect(Collectors.toSet());
    }

    private boolean filterOps(String path, PathItem pi) {

        Function<PathItem.HttpMethod, Operation> getOp = m -> switch (m) {
            case GET -> pi.getGet();
            case PUT -> pi.getPut();
            case HEAD -> pi.getHead();
            case POST -> pi.getPost();
            case PATCH -> pi.getPatch();
            case TRACE -> pi.getTrace();
            case DELETE -> pi.getDelete();
            case OPTIONS -> pi.getOptions();
        };

        Consumer<PathItem.HttpMethod> nullifyOp = m -> {
            switch (m) {
                case GET -> pi.setGet(null);
                case PUT -> pi.setPut(null);
                case HEAD -> pi.setHead(null);
                case POST -> pi.setPost(null);
                case PATCH -> pi.setPatch(null);
                case TRACE -> pi.setTrace(null);
                case DELETE -> pi.setDelete(null);
                case OPTIONS -> pi.setOptions(null);
            }
        };

        boolean hasOpsLeft = false;
        for (PathItem.HttpMethod m : PathItem.HttpMethod.values()) {
            if (getOp.apply(m) != null) {
                if (!allowPathAndMethodCheck.test(path, m)) {
                    nullifyOp.accept(m);
                } else {
                    hasOpsLeft = true;
                }
            }
        }

        return hasOpsLeft;
    }

    private static Set<String> schemaRefsInUse(OpenAPI api) {

        Set<String> refsInUse = new HashSet<>();
        Map<String, Schema> schemas = api.getComponents().getSchemas();

        // search for schema references
        if (api.getPaths() != null) {
            api.getPaths().values().stream()
                    .flatMap(pi -> Stream.of(pi.getGet(), pi.getPost(), pi.getPut()))

                    .filter(o -> o != null && o.getResponses() != null)
                    .flatMap(o -> o.getResponses().values().stream())

                    .forEach(ar -> appendSchemaRefs(schemas, refsInUse, ar));
        }

        return refsInUse;
    }

    private static void appendSchemaRefs(Map<String, Schema> schemas, Set<String> refsInUse, ApiResponse ar) {
        if (ar.get$ref() != null) {
            appendSchemaAndDependents(schemas, refsInUse, ar.get$ref());
        }

        if (ar.getContent() != null) {
            ar.getContent().values().stream()

                    // we don't care for full schemas, only for those that are refs to schemas in components
                    .filter(mt -> mt.getSchema() != null && mt.getSchema().get$ref() != null)
                    .forEach(mt -> appendSchemaAndDependents(schemas, refsInUse, mt.getSchema().get$ref()));
        }
    }

    private static void appendSchemaAndDependents(Map<String, Schema> schemas, Set<String> schemaRefs, String schemaRef) {

        if (!schemaRef.startsWith(SCHEMA_REF_PREFIX)) {
            throw new IllegalStateException("Unrecognized schema ref format: " + schemaRef);
        }

        String schemaName = schemaRef.substring(SCHEMA_REF_PREFIX.length());

        // if not seen yet, descend into children
        if (schemaRefs.add(schemaName)) {
            appendDependents(schemas, schemaRefs, schemas.get(schemaName));
        }
    }

    private static void appendDependents(Map<String, Schema> schemas, Set<String> schemaRefs, Schema<?> schema) {

        if (schema == null) {
            return;
        }

        if ("array".equals(schema.getType())) {

            // array elements are declared here
            appendDependents(schemas, schemaRefs, schema.getItems());

        } else if ("object".equals(schema.getType())) {

            // map elements are declared here; oddly enough "getAdditionalProperties()" returns Object, not schema
            if (schema.getAdditionalProperties() instanceof Schema ap) {
                appendDependents(schemas, schemaRefs, ap);
            }
        }

        // TODO: process schema.getAdditionalItems()? how do they get there?

        if (schema.get$ref() != null) {
            appendSchemaAndDependents(schemas, schemaRefs, schema.get$ref());
        }

        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(s -> appendDependents(schemas, schemaRefs, s));
        }
    }
}
