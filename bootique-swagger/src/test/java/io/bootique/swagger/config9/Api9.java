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
package io.bootique.swagger.config9;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("api9")
public class Api9 {

    @GET
    @Path("deprecated-object")
    public ODeprecated oDeprecated() {
        return null;
    }

    @GET
    @Path("deprecated-object-property")
    public OPropDeprecated oPropDeprecated() {
        return null;
    }
}
