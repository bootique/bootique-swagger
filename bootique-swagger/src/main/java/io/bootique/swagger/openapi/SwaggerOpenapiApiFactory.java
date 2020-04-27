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

import io.bootique.jersey.MappedResource;

import javax.inject.Provider;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @since 2.0
 */
// notice that this is not a BQConfig factory, and it is created manually,
// as we'd like to avoid "consoles" property exposure in YAML.
// TODO: some Jackson trick to derserialize this as a map?
public class SwaggerOpenapiApiFactory {

    private Map<String, OpenApiModelFactory> modelFactories;

    public SwaggerOpenapiApiFactory(Map<String, OpenApiModelFactory> modelFactories) {
        // if nothing is mapped, still generate OpenAPI with default configuration
        this.modelFactories = modelFactories == null || modelFactories.isEmpty()
                ? Collections.singletonMap("default", defaultApiFactory())
                : modelFactories;
    }

    public MappedResource<SwaggerOpenapiApi> createResource(Provider<? extends Application> appProvider) {

        Map<String, OpenApiModel> models = new HashMap<>();
        modelFactories.values()
                .stream()
                .map(f -> f.createModel(appProvider))
                // skip unmapped models
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(m -> indexByPath(models, m));

        SwaggerOpenapiApi resource = new SwaggerOpenapiApi(models);
        return new MappedResource<>(resource, models.keySet());
    }

    private void indexByPath(Map<String, OpenApiModel> models, OpenApiModel model) {
        if (model.getPathJson() != null) {
            models.put(model.getPathJson(), model);
        }

        if (model.getPathYaml() != null) {
            models.put(model.getPathYaml(), model);
        }
    }

    protected OpenApiModelFactory defaultApiFactory() {
        return new OpenApiModelFactory();
    }
}
