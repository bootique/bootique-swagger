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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @since 2.0
 */
public class OpenApiLoader {

    public OpenAPI load(List<String> resourcePackages, List<String> resourceClasses, URL specLocation, URL overrideSpecLocation) {

        // override order
        // 1. class annotations
        // 2. spec (overrides annotations)
        // 3. override spec (overrides spec and annotations)

        OpenAPI empty = new OpenAPI();

        OpenAPI specFromAnnotations = !resourcePackages.isEmpty() || !resourceClasses.isEmpty()
                ? loadSpecFromAnnotations(empty, resourcePackages, resourceClasses) : empty;
        OpenAPI spec = specLocation != null ? OpenApiMerger.merge(specFromAnnotations, loadSpec(specLocation)) : specFromAnnotations;
        OpenAPI specOverride = overrideSpecLocation != null ? OpenApiMerger.merge(spec, loadSpec(overrideSpecLocation)) : spec;

        // sort paths for stable specs... OpenAPI loads them in different order depending on the JVM version
        OpenAPI pathsSorted = sortPaths(specOverride);
        return sortSchemas(pathsSorted);
    }

    protected OpenAPI loadSpecFromAnnotations(OpenAPI mergeInto, List<String> resourcePackages, List<String> resourceClasses) {

        SwaggerConfiguration config = new SwaggerConfiguration();
        config.setOpenAPI(mergeInto);

        if (!resourcePackages.isEmpty()) {
            config.setResourcePackages(new HashSet<>(resourcePackages));
        }

        if (!resourceClasses.isEmpty()) {
            config.setResourceClasses(new HashSet<>(resourceClasses));
        }

        JaxrsAnnotationScanner scanner = new JaxrsAnnotationScanner();
        scanner.setConfiguration(config);

        Reader reader = new Reader();
        reader.setConfiguration(config);
        return reader.read(scanner.classes(), scanner.resources());
    }

    protected OpenAPI sortPaths(OpenAPI api) {

        Paths paths = api.getPaths();
        if (paths == null || paths.keySet().size() < 2) {
            return api;
        }

        Paths sorted = new Paths();
        paths.keySet().stream().sorted().forEach(p -> sorted.put(p, paths.get(p)));
        api.setPaths(sorted);

        return api;
    }

    protected OpenAPI sortSchemas(OpenAPI api) {

        Components components = api.getComponents();
        if (components == null) {
            return api;
        }

        Map<String, Schema> schemas = components.getSchemas();
        if (schemas == null || schemas.size() < 2) {
            return api;
        }

        LinkedHashMap<String, Schema> sorted = new LinkedHashMap<>();
        schemas.keySet().stream().sorted().forEach(s -> sorted.put(s, schemas.get(s)));
        components.setSchemas(sorted);
        
        return api;
    }

    protected OpenAPI loadSpec(URL location) {

        ObjectMapper mapper = createMapper(location);
        try {
            return mapper.readValue(location, OpenAPI.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration from " + location, e);
        }
    }

    protected ObjectMapper createMapper(URL location) {
        return location.getFile().toLowerCase().endsWith(".json")
                ? Json.mapper()
                : Yaml.mapper();
    }
}
