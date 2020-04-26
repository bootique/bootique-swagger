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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @since 2.0
 */
@BQConfig
public class OpenApiModelFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiModelFactory.class);

    private String pathJson;
    private String pathYaml;
    private ResourceFactory staticLocation;
    private List<String> resourcePackages;

    public Optional<OpenApiModel> createModel(Provider<? extends Application> appProvider) {

        List<String> specPaths = createSpecPaths();
        if (specPaths.isEmpty()) {
            LOGGER.info("Neither 'pathJson' not 'pathYaml' are set. Skipping OpenApiModel creation");
            return Optional.empty();
        }

        return Optional.of(new OpenApiModel(() -> createOpenApi(appProvider.get()), specPaths));
    }

    protected List<String> createSpecPaths() {
        if (pathJson == null && pathYaml == null) {
            return Collections.emptyList();
        }

        List<String> paths = new ArrayList<>(2);
        if (pathJson != null) {
            paths.add(pathJson);
        }

        if (pathYaml != null) {
            paths.add(pathYaml);
        }

        return paths;
    }

    protected OpenAPI createOpenApi(Application application) {

        JaxrsOpenApiContextBuilder builder = new JaxrsOpenApiContextBuilder();

        String staticLocation = createStaticLocation();
        if (staticLocation != null) {
            builder.configLocation(staticLocation);
        }

        if (resourcePackages != null) {
            builder.setApplication(application);
            builder.resourcePackages(new HashSet<>(resourcePackages));
        }

        try {
            return builder
                    // suppose ID is needed as the builder keeps contexts in the static cache and we want to avoid
                    // context reuse between factories.
                    // TODO: Ideally we should avoid caching all together, as it looks like an uneeded memory leak
                    .ctxId(pseudoUniqueContextId())
                    .buildContext(true)
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException("Error loading OpenAPI config", e);
        }
    }

    protected String pseudoUniqueContextId() {
        return "io.bootique.swagger.openapi.OpenApiModelFactory_" + this.hashCode();
    }

    protected String createStaticLocation() {
        if (staticLocation == null) {
            return null;
        }

        // Since JaxrsOpenApiContextBuilder will resolve location on its own, strip "classpath:" prefix
        String resourceId = staticLocation.getResourceId();
        return resourceId.startsWith("classpath:") ? resourceId.substring("classpath:".length()) : resourceId;
    }

    @BQConfigProperty
    public void setResourcePackages(List<String> resourcePackages) {
        this.resourcePackages = resourcePackages;
    }

    @BQConfigProperty
    public void setPathJson(String pathJson) {
        this.pathJson = pathJson;
    }

    @BQConfigProperty
    public void setPathYaml(String pathYaml) {
        this.pathYaml = pathYaml;
    }

    @BQConfigProperty
    public void setStaticLocation(ResourceFactory staticLocation) {
        this.staticLocation = staticLocation;
    }
}
