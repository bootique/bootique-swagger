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
package io.bootique.swagger.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.integration.JaxrsApplicationAndAnnotationScanner;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

/**
 * @since 2.0
 */
public class OpenApiLoader {

    private Application application;

    public OpenApiLoader(Application application) {
        this.application = application;
    }

    public OpenAPI load(List<String> resourcePackages, URL specLocation, URL overrideSpecLocation) {

        // override order
        // 1. class annotations
        // 2. spec (overrides annotations)
        // 3. override spec (overrides spec and annotations)

        OpenAPI empty = new OpenAPI();
        OpenAPI specFromAnnotations = resourcePackages != null ? loadSpecFromAnnaotations(empty, resourcePackages) : empty;
        OpenAPI spec = specLocation != null ? loadSpec(specFromAnnotations, specLocation) : specFromAnnotations;
        OpenAPI specOverride = overrideSpecLocation != null ? loadSpec(spec, overrideSpecLocation) : spec;

        return specOverride;
    }

    protected OpenAPI loadSpecFromAnnaotations(OpenAPI mergeInto, List<String> resourcePackages) {

        SwaggerConfiguration config = new SwaggerConfiguration();
        config.setOpenAPI(mergeInto);
        config.setResourcePackages(new HashSet<>(resourcePackages));

        JaxrsApplicationAndAnnotationScanner scanner = new JaxrsApplicationAndAnnotationScanner();
        scanner.setConfiguration(config);
        scanner.setApplication(application);

        Reader reader = new Reader();
        reader.setConfiguration(config);
        reader.setApplication(application);
        return reader.read(scanner.classes(), scanner.resources());
    }

    protected OpenAPI loadSpec(OpenAPI mergeInto, URL location) {

        ObjectMapper mapper = createMapper(location);
        try {
            return mapper.readerForUpdating(mergeInto).readValue(location);
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
