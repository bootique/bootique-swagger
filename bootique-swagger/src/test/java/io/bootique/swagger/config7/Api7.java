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
package io.bootique.swagger.config7;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("api7")
public class Api7 {

    // intentionally naming the endpoints in a different alphabetic order vs their returned schemas
    // and in a random order within the endpoint

    @GET
    @Path("9")
    public OA _9() {
        return null;
    }

    @GET
    @Path("1")
    public OZ _1() {
        return null;
    }

    @GET
    @Path("7")
    public OB _7() {
        return null;
    }
}
