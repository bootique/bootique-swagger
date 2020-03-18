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

package io.bootique.swagger.ui;

import io.bootique.jersey.JerseyModule;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class OpenapiUiDefaultLocationIT extends SwaggerUiBaseIT {

	@BeforeClass
	public static void beforeClass() {
		TEST_FACTORY.app("-s")
				.args("-c", "classpath:test_openapi.yml")
				.module(new io.bootique.openapi.OpenapiModuleProvider())
				.module(new SwaggerUiModuleProvider())
				.module(b -> JerseyModule.extend(b).addResource(TestApi.class))
				.run();
	}

	@Test
	public void testApi_Console() {
		Response r = BASE_TARGET.path("/swagger").request().get();
		assertEquals(200, r.getStatus());
		assertEqualsToResourceContents("openapi-response1.html", r.readEntity(String.class));
	}

}
