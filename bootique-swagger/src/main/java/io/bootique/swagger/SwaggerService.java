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

import java.util.Map;
import java.util.Set;

/**
 * @since 3.0.M2
 */
public class SwaggerService {

    private final Map<String, OpenApiModel> models;

    public SwaggerService(Map<String, OpenApiModel> models) {
        this.models = models;
    }

    public OpenApiModel getOpenApiModel(String path) {
        OpenApiModel model = models.get(path);

        if (model == null) {
            throw new IllegalStateException("Can't find OpenApi model by path " + path);
        }
        
        return model;
    }

    /**
     * Returns a full set of preconfigured API model URL paths.
     */
    public Set<String> getUrlPatterns() {
        return models.keySet();
    }
}