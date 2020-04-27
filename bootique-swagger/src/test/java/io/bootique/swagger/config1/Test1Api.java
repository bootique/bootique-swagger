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
package io.bootique.swagger.config1;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(info = @Info(
        title = "This must be ignored",
        description = "This must be ignored",
        version = "_ignored_"))
@Path("config1/test1")
public class Test1Api {

    @GET
    @Operation(tags = {"a"})
    public Response get() {
        return Response.ok("test1").build();
    }

    @PUT
    @Operation(tags = {"b"})
    public Response put(String entity) {
        return Response.ok("test1").build();
    }

    @GET
    @Path("/sub/{id}")
    public String subget(@PathParam("id") int id) {
        return "get_" + id;
    }
}
