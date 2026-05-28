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
package io.bootique.swagger.config14;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("api14")
public class Api14 {

    // intentionally tag in reverse-alphabetical order to verify sorting
    @GET
    @Path("1")
    @Operation(tags = {"z"})
    public void e1() {
    }

    @GET
    @Path("2")
    @Operation(tags = {"m"})
    public void e2() {
    }

    @GET
    @Path("3")
    @Operation(tags = {"a"})
    public void e3() {
    }
}
