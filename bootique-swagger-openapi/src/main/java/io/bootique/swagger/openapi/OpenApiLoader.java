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

    public static OpenAPI load(URL baseSpecLocation, List<String> resourcePackages, Application app) {
        OpenAPI baseSpec = baseSpecLocation != null ? loadBaseSpec(baseSpecLocation) : new OpenAPI();
        OpenAPI specWithResources = resourcePackages != null ? mergeAnnotationSpec(baseSpec, resourcePackages, app) : baseSpec;
        return specWithResources;
    }

    protected static OpenAPI mergeAnnotationSpec(OpenAPI baseApi, List<String> resourcePackages, Application app) {

        SwaggerConfiguration config = new SwaggerConfiguration();
        config.setOpenAPI(baseApi);
        config.setResourcePackages(new HashSet<>(resourcePackages));

        JaxrsApplicationAndAnnotationScanner scanner = new JaxrsApplicationAndAnnotationScanner();
        scanner.setConfiguration(config);
        scanner.setApplication(app);

        Reader reader = new Reader();
        reader.setConfiguration(config);
        reader.setApplication(app);
        return reader.read(scanner.classes(), scanner.resources());
    }

    protected static OpenAPI loadBaseSpec(URL specOverrideLocation) {

        ObjectMapper mapper = specOverrideLocation.getFile().toLowerCase().endsWith(".json")
                ? Json.mapper()
                : Yaml.mapper();

        try {
            return mapper.readValue(specOverrideLocation, OpenAPI.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration from " + specOverrideLocation, e);
        }
    }
}
