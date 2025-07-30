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

import io.swagger.v3.oas.models.OpenAPI;

import java.util.function.Supplier;

/**
 * An in-place customizer of the mutable OpenAPI model object.
 *
 * @since 3.0
 */
public interface OpenApiCustomizer {

    /**
     * A callback invoked by Bootique to allow this customizer to inspect and alter the provided Open API model.
     * Calling "apiSupplier.get()" within the customizer returns a mutable copy of the {@link OpenAPI}
     * object that can be changed directly.
     *
     * @since 4.0
     */
    // passing Supplier<OpenAPI> instead of just OpenAPI gives the *caller* control over scoping the provided instance.
    // E.g., it might lazily clone a shared OpenAPI instance to limit customizations to a single request scope without
    // messing up the shared object
    void customize(String name, Supplier<OpenAPI> apiSupplier);
}
