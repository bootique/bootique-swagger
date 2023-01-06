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
package io.bootique.swagger.service;

import io.bootique.resource.ResourceFactory;
import io.bootique.swagger.OpenApiLoader;
import io.bootique.swagger.OpenApiModel;
import io.swagger.v3.oas.models.OpenAPI;

import javax.inject.Provider;
import javax.ws.rs.core.Application;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;

public class SwaggerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerService.class);
    private final Provider<? extends Application> appProvider;
    private final SwaggerConfig swaggerConfig;
    private final Map<String, OpenApiModel> models;

    public SwaggerService(Provider<? extends Application> appProvider, SwaggerConfig swaggerConfig) {
        this.swaggerConfig = swaggerConfig;
        this.models = convertSpecs(swaggerConfig.getSpecs());
        this.appProvider = appProvider;
    }

    public Optional<OpenApiModel> getOpenApiModel(String path) {
        return Optional.ofNullable(models.get(path));
    }

    private Map<String, OpenApiModel> convertSpecs(Map<String, SwaggerSpec> specs) {
        Map<String, OpenApiModel> models = new HashMap<>();
        specs.values().stream()
                .map(swaggerSpec -> createModel(swaggerConfig, swaggerSpec))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(m -> indexByPath(models, m));
        return models;
    }

    private void indexByPath(Map<String, OpenApiModel> models, OpenApiModel model) {
        if (model.getPathJson() != null) {
            models.put(model.getPathJson(), model);
        }

        if (model.getPathYaml() != null) {
            models.put(model.getPathYaml(), model);
        }
    }

    private Optional<OpenApiModel> createModel(SwaggerConfig config, SwaggerSpec swaggerSpec) {
        if (swaggerSpec.getPathJson() == null &&
                swaggerSpec.getPathYaml() == null) {
            LOGGER.info("Neither 'pathJson' nor 'pathYaml' are set. Skipping OpenApiModel creation");
            return Optional.empty();
        }

        // capture values for the lambda args
        URL spec = resolveSpecUrl(swaggerSpec.getSpec());
        URL overrideSpec = resolveOverrideSpecUrl(swaggerSpec.getOverrideSpec());
        String pathJson = normalizePath(swaggerSpec.getPathJson());
        String pathYaml = normalizePath(swaggerSpec.getPathYaml());

        return Optional.of(new OpenApiModel(
                () -> createOpenApi(appProvider.get(), spec, overrideSpec, swaggerSpec),
                pathJson,
                pathYaml,
                config.isPretty()));
    }

    protected String normalizePath(String path) {
        return path != null && path.startsWith("/") ? path.substring(1) : path;
    }

    protected OpenAPI createOpenApi(Application app, URL spec, URL overrideSpec, SwaggerSpec swaggerSpec) {

        // our own implementation. JaxrsOpenApiContextBuilder is too dirty and unpredictable, and not easy to
        // extend to do our own config merging
        // todo move checks to getters?
        List<String> resourcePackages = Objects.requireNonNullElse(swaggerSpec.getResourcePackages(), emptyList());
        List<String> resourceClasses = Objects.requireNonNullElse(swaggerSpec.getResourceClasses(), emptyList());

        return new OpenApiLoader(app).load(resourcePackages, resourceClasses, spec, overrideSpec);
    }

    protected URL resolveOverrideSpecUrl(ResourceFactory overrideSpec) {
        ResourceFactory spec = overrideSpec != null ? overrideSpec : swaggerConfig.getOverrideSpec();
        return spec != null ? spec.getUrl() : null;
    }

    protected URL resolveSpecUrl(ResourceFactory spec) {
        return spec != null ? spec.getUrl() : null;
    }

    public Set<String> getUrlPatterns() {
        return models.keySet();
    }
}