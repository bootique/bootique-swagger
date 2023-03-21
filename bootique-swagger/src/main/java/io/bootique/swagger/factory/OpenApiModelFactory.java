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
package io.bootique.swagger.factory;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.swagger.OpenApiCustomizer;
import io.bootique.swagger.OpenApiLoader;
import io.bootique.swagger.OpenApiModel;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.ws.rs.core.Application;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@BQConfig
public class OpenApiModelFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiModelFactory.class);

    private String pathJson;
    private String pathYaml;
    private ResourceFactory overrideSpec;
    private ResourceFactory spec;
    private List<String> resourcePackages;
    private List<String> resourceClasses;
    private boolean noWebAccess;

    public Optional<OpenApiModel> createModel(
            String modelName,
            ResourceFactory sharedOverrideSpec,
            boolean prettyPrint,
            List<OpenApiCustomizer> customizers) {

        if (pathJson == null && pathYaml == null) {
            LOGGER.info("Neither 'pathJson' not 'pathYaml' are set. Skipping OpenApiModel creation");
            return Optional.empty();
        }

        // capture values for the lambda args
        URL spec = resolveSpec();
        URL overrideSpec = resolveOverrideSpec(sharedOverrideSpec);
        String pathJson = normalizePath(this.pathJson);
        String pathYaml = normalizePath(this.pathYaml);

        return Optional.of(new OpenApiModel(
                () -> createOpenApi(modelName, spec, overrideSpec, customizers),
                pathJson,
                pathYaml,
                prettyPrint,
                noWebAccess));
    }

    protected String normalizePath(String path) {
        return path != null && path.startsWith("/") ? path.substring(1) : path;
    }

    protected OpenAPI createOpenApi(String modelName, URL spec, URL overrideSpec, List<OpenApiCustomizer> customizers) {

        // our own implementation. JaxrsOpenApiContextBuilder is too dirty and unpredictable, and not easy to
        // extend to do our own config merging

        List<String> resourcePackages = this.resourcePackages != null ? this.resourcePackages : List.of();
        List<String> resourceClasses = this.resourceClasses != null ? this.resourceClasses : List.of();

        OpenAPI api = new OpenApiLoader().load(resourcePackages, resourceClasses, spec, overrideSpec);
        customizers.forEach(c -> c.customize(modelName, api));
        return api;
    }

    protected URL resolveOverrideSpec(ResourceFactory sharedOverrideSpec) {
        ResourceFactory spec = this.overrideSpec != null ? this.overrideSpec : sharedOverrideSpec;
        return spec != null ? spec.getUrl() : null;
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

    /**
     * @since 3.0
     */
    @BQConfigProperty("Whether to disable web access to this OpenAPI spec. In that case the descriptors will still be " +
            "accessible offline via '--generate-spec' command. 'false' by default.")
    public void setNoWebAccess(boolean noWebAccess) {
        this.noWebAccess = noWebAccess;
    }
}
