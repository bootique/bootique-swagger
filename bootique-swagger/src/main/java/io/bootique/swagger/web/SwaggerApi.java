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
package io.bootique.swagger.web;

import io.bootique.swagger.OpenApiCustomizer;
import io.bootique.swagger.OpenApiModel;
import io.bootique.swagger.OpenApiRequestCustomizer;
import io.bootique.swagger.SwaggerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Path("_this_is_a_placeholder_that_will_be_replaced_dynamically_")
public class SwaggerApi {

    private static final String MEDIA_TYPE_JSON = MediaType.APPLICATION_JSON;
    private static final String MEDIA_TYPE_YAML = "application/yaml";

    private final SwaggerService service;
    private final Set<OpenApiRequestCustomizer> customizers;

    public SwaggerApi(SwaggerService service, Set<OpenApiRequestCustomizer> customizers) {
        this.service = service;
        this.customizers = customizers;
    }

    @GET
    @Produces({MEDIA_TYPE_JSON, MEDIA_TYPE_YAML})
    @Operation(hidden = true)
    public Response getOpenApi(@Context HttpServletRequest request, @Context UriInfo uriInfo) {

        String path = uriInfo.getPath();

        List<OpenApiCustomizer> requestCustomizers = customizers
                .stream()
                .map(c -> requestCustomizer(request, c))
                .toList();

        OpenApiModel model = service.getOpenApiModel(path).customize(path, requestCustomizers);

        return model.noWebAccess()
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.status(Response.Status.OK).entity(model.render(path)).type(getMediaType(path, model)).build();
    }

    private String getMediaType(String path, OpenApiModel model) {
        return Objects.equals(model.getPathJson(), path) ? MEDIA_TYPE_JSON : MEDIA_TYPE_YAML;
    }

    private static OpenApiCustomizer requestCustomizer(HttpServletRequest request, OpenApiRequestCustomizer customizer) {
        return (n, s) -> customizer.customize(request, s);
    }
}
