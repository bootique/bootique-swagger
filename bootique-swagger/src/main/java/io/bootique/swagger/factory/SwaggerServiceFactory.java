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
import io.bootique.swagger.OpenApiModel;
import io.bootique.swagger.SwaggerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@BQConfig
public class SwaggerServiceFactory {

    private ResourceFactory overrideSpec;
    private Map<String, OpenApiModelFactory> specs;
    private boolean pretty = true;
    private boolean noWebAccess;

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

    @BQConfigProperty("Whether to disable web access to OpenAPI descriptors. In this case the descriptors will still be " +
            "accessible offline via '--generate-spec' command. This is 'false' by default.")
    public void setNoWebAccess(boolean noWebAccess) {
        this.noWebAccess = noWebAccess;
    }

    public SwaggerService createSwaggerService() {
        var models = createModels(this.specs);
        return new SwaggerService(models, noWebAccess);
    }

    private Map<String, OpenApiModel> createModels(Map<String, OpenApiModelFactory> specs) {
        Map<String, OpenApiModel> models = new HashMap<>();
        if (specs == null) {
            specs = new HashMap<>();
        }
        specs.values().stream()
                .map(swaggerSpec -> swaggerSpec.createModel(overrideSpec, pretty))
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
}
