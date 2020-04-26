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

import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public class OpenApiModel {

    private Supplier<OpenAPI> apiSupplier;
    private List<String> specPaths;
    private volatile OpenAPI api;

    public OpenApiModel(Supplier<OpenAPI> apiSupplier, List<String> specPaths) {
        this.specPaths = specPaths;
        this.apiSupplier = apiSupplier;
    }

    public List<String> getSpecPaths() {
        return specPaths;
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
}
