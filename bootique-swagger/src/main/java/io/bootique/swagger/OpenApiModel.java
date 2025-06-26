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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public class OpenApiModel {

    private final boolean pretty;
    private final boolean noWebAccess;
    private final Supplier<OpenAPI> apiSupplier;
    private final String pathJson;
    private final String pathYaml;
    private volatile OpenAPI api;

    public OpenApiModel(Supplier<OpenAPI> apiSupplier, String pathJson, String pathYaml, boolean pretty, boolean noWebAccess) {
        this.pathJson = pathJson;
        this.pathYaml = pathYaml;
        this.apiSupplier = apiSupplier;
        this.pretty = pretty;
        this.noWebAccess = noWebAccess;
    }

    public boolean isPretty() {
        return pretty;
    }

    /**
     * @since 3.0
     */
    public boolean noWebAccess() {
        return noWebAccess;
    }

    public String getPathJson() {
        return pathJson;
    }

    public String getPathYaml() {
        return pathYaml;
    }

    public OpenAPI getApi() {

        // lazy resolving OpenAPI, as it depends on Application that is not available when the resource is created
        if (api == null) {
            synchronized (this) {
                if (api == null) {
                    api = apiSupplier.get();
                }
            }
        }
        return api;
    }

    public String render(String path) {
        OpenAPI api = getApi();
        return Objects.equals(pathJson, path) ? printJson(api) : printYaml(api);
    }

    private String printJson(OpenAPI api) {
        try {
            return this.isPretty() ? Json.pretty(api) : Json.mapper().writeValueAsString(api);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting model to JSON", e);
        }
    }

    private String printYaml(OpenAPI api) {
        try {
            return this.isPretty() ? Yaml.pretty(api) : Yaml.mapper().writeValueAsString(api);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting model to JSON", e);
        }
    }
}
