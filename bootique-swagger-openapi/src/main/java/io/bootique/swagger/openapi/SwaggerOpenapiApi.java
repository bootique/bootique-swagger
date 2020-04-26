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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.annotations.Operation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

@Path("_this_is_a_placeholder_that_will_be_replaced_dynamically_")
public class SwaggerOpenapiApi {

    private Map<String, OpenApiModel> models;

    public SwaggerOpenapiApi(Map<String, OpenApiModel> models) {
        this.models = models;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    @Operation(hidden = true)
    public Response getOpenApi(@Context UriInfo uriInfo) {

        String path = uriInfo.getPath();
        OpenApiModel oai = models.get(path);
        if (oai == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .entity("No model at " + path).build();
        }

        String type = responseType(path);
        switch (type) {
            case "json":
                return jsonResponse(oai);
            case "yaml":
                return yamlResponse(oai);
            default:
                throw new RuntimeException("Should never get here");
        }
    }

    protected String responseType(String path) {
        // TODO: we don't need to guess... Match this with "pathJson" and "pathYaml" from the factory.
        //  Otherwise user's failure to use ".json" extension will prevent the resource from being accessible
        return path.endsWith(".json") ? "json" : "yaml";
    }

    protected Response yamlResponse(OpenApiModel model) {

        return Response.status(Response.Status.OK)
                .entity(printYaml(model))
                .type("application/yaml")
                .build();
    }

    protected Response jsonResponse(OpenApiModel model) {
        return Response.status(Response.Status.OK)
                .entity(printJson(model))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    protected String printYaml(OpenApiModel model) {
        try {
            return model.isPretty() ? Yaml.pretty(model.getApi()) : Yaml.mapper().writeValueAsString(model.getApi());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting model to YAML", e);
        }
    }

    protected String printJson(OpenApiModel model) {
        try {
            return model.isPretty() ? Json.pretty(model.getApi()) : Json.mapper().writeValueAsString(model.getApi());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting model to JSON", e);
        }
    }
}
