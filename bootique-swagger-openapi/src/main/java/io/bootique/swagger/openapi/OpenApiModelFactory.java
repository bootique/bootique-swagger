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
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.ws.rs.core.Application;
import java.net.URL;
import java.util.Collections;
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
    private ResourceFactory overrideSpec;
    private ResourceFactory spec;
    private List<String> resourcePackages;
    private List<String> resourceClasses;
    private boolean pretty = true;

    public Optional<OpenApiModel> createModel(Provider<? extends Application> appProvider) {

        if (pathJson == null && pathYaml == null) {
            LOGGER.info("Neither 'pathJson' not 'pathYaml' are set. Skipping OpenApiModel creation");
            return Optional.empty();
        }

        // capture values for the lambda args
        URL spec = resolveSpec();
        URL overrideSpec = resolveOverrideSpec();
        boolean pretty = this.pretty;
        String pathJson = normalizePath(this.pathJson);
        String pathYaml = normalizePath(this.pathYaml);

        return Optional.of(new OpenApiModel(() -> createOpenApi(appProvider.get(), spec, overrideSpec), pathJson, pathYaml, pretty));
    }

    protected String normalizePath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    protected OpenAPI createOpenApi(Application app, URL spec, URL overrideSpec) {

        // our own implementation. JaxrsOpenApiContextBuilder is too dirty and unpredictable, and not easy to
        // extend to do our own config merging

        List<String> resourcePackages = this.resourcePackages != null ? this.resourcePackages : Collections.emptyList();
        List<String> resourceClasses = this.resourceClasses != null ? this.resourceClasses : Collections.emptyList();

        return new OpenApiLoader(app).load(resourcePackages, resourceClasses, spec, overrideSpec);
    }

    protected URL resolveOverrideSpec() {
        return overrideSpec != null ? overrideSpec.getUrl() : null;
    }

    protected URL resolveSpec() {
        return spec != null ? spec.getUrl() : null;
    }

    @BQConfigProperty("An optional list of Java packages that contain annotated API endpoint classes")
    public void setResourcePackages(List<String> resourcePackages) {
        this.resourcePackages = resourcePackages;
    }

    @BQConfigProperty("An optional list of Java classes for the annotated API endpoints")
    public void setResourceClasses(List<String> resourceClasses) {
        this.resourceClasses = resourceClasses;
    }

    @BQConfigProperty("Publishes an OpenAPI metadata endpoint as JSON at the specified path")
    public void setPathJson(String pathJson) {
        this.pathJson = pathJson;
    }

    @BQConfigProperty("Publishes an OpenAPI metadata endpoint as YAML at the specified path")
    public void setPathYaml(String pathYaml) {
        this.pathYaml = pathYaml;
    }

    @BQConfigProperty("Location of the OpenAPI spec file. Overrides 'spec', 'resourcePackages', 'resourceClasses' models")
    public void setOverrideSpec(ResourceFactory overrideSpec) {
        this.overrideSpec = overrideSpec;
    }

    @BQConfigProperty("Location of the OpenAPI spec file. Overrides 'resourcePackages' and 'resourceClasses' model")
    public void setSpec(ResourceFactory spec) {
        this.spec = spec;
    }

    @BQConfigProperty("Whether to format YAML and JSON. 'True' by default")
    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }
}
