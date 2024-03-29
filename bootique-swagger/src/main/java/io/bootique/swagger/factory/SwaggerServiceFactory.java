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
import io.bootique.swagger.OpenApiModel;
import io.bootique.swagger.SwaggerService;
import io.bootique.swagger.customizer.PathSortingCustomizer;
import io.bootique.swagger.customizer.SchemasSortingCustomizer;

import java.util.*;

/**
 * @deprecated in favor of the Jakarta flavor
 */
@Deprecated(since = "3.0", forRemoval = true)
@BQConfig
public class SwaggerServiceFactory {

    private ResourceFactory overrideSpec;
    private Map<String, OpenApiModelFactory> specs;
    private boolean pretty = true;

    @BQConfigProperty("Zero or more API specifications provided by the application")
    public void setSpecs(Map<String, OpenApiModelFactory> specs) {
        this.specs = specs;
    }

    @BQConfigProperty("Location of the OpenAPI spec file, that overrides 'spec', 'resourcePackages', 'resourceClasses' models. " +
            "This setting is shared by all child specs, unless they define an explicit 'overrideSpec' of their own")
    public void setOverrideSpec(ResourceFactory overrideSpec) {
        this.overrideSpec = overrideSpec;
    }

    @BQConfigProperty("Whether to format YAML and JSON. 'True' by default. This setting is shared by all child specs.")
    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    public SwaggerService createSwaggerService(Set<OpenApiCustomizer> customizers) {
        return new SwaggerService(createModels(specs, prepareCustomizers(customizers)));
    }

    private List<OpenApiCustomizer> prepareCustomizers(Set<OpenApiCustomizer> diCustomizers) {

        // start with standard customizers, so that the user-provided ones can fix the model to
        // their liking

        List<OpenApiCustomizer> customizers = new ArrayList<>(2 + diCustomizers.size());
        customizers.add(new PathSortingCustomizer());
        customizers.add(new SchemasSortingCustomizer());
        customizers.addAll(diCustomizers);

        return customizers;
    }

    private Map<String, OpenApiModel> createModels(
            Map<String, OpenApiModelFactory> specs,
            List<OpenApiCustomizer> customizers) {

        Map<String, OpenApiModel> models = new HashMap<>();

        Map<String, OpenApiModelFactory> localSpecs = specs != null ? specs : new HashMap<>();
        localSpecs.forEach((n, f) -> loadModel(models, n, f, customizers));

        return models;
    }

    private void loadModel(
            Map<String, OpenApiModel> models,
            String name,
            OpenApiModelFactory factory,
            List<OpenApiCustomizer> customizers) {
        factory.createModel(name, overrideSpec, pretty, customizers).ifPresent(m -> indexByPath(models, m));
    }

    private void indexByPath(Map<String, OpenApiModel> models, OpenApiModel model) {
        if (model.getPathJson() != null) {
            models.put(model.getPathJson(), model);
        }

        if (model.getPathYaml() != null) {
            models.put(model.getPathYaml(), model);
        }
    }
}
