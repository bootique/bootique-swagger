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

import io.bootique.jersey.JerseyModule;
import io.bootique.swagger.config1.Test1Api;
import io.bootique.test.junit5.BQTestClassFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationSpec_YamlOverrideSpec_IT {

    @RegisterExtension
    public static final BQTestClassFactory testFactory = new BQTestClassFactory();
    private static final WebTarget target = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @BeforeAll
    public static void beforeClass() {
        testFactory.app("-s", "-c", "classpath:config1/startup.yml")
                .autoLoadModules()
                .module(b -> JerseyModule.extend(b).addResource(Test1Api.class))
                .run();
    }

    @Test
    public void testYaml() {

        Response r = target.path("/s1/model.yaml").request().get();
        assertEquals(200, r.getStatus());
        SwaggerAsserts.assertEqualsToResource(r.readEntity(String.class), "config1/response.yml");
    }

    @Test
    public void testJson() {

        Response r = target.path("/s1/model.json").request().get();
        assertEquals(200, r.getStatus());
        SwaggerAsserts.assertEqualsToResource(r.readEntity(String.class), "config1/response.json");
    }
}
