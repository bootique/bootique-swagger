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

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.MappedServlet;
import io.bootique.swagger.ui.mustache.SwaggerUiMustacheServlet;

import java.net.URL;

/**
 * @since  1.0.RC1
 */
public class SwaggerUiModule extends ConfigModule {

	private static final String RESOURCE_BASE = "bq.jetty.servlets.static.params.resourceBase";
	private static final String PATH_INFO_ONLY = "bq.jetty.servlets.static.params.pathInfoOnly";

	@Override
	public void configure(Binder binder) {
		URL resource = this.getClass().getClassLoader().getResource("swagger-ui/static/");
		BQCoreModule.extend(binder).setProperty(RESOURCE_BASE, resource.toString());
		BQCoreModule.extend(binder).setProperty(PATH_INFO_ONLY, "true");
		JettyModule.extend(binder).addMappedServlet(new TypeLiteral<MappedServlet<SwaggerUiMustacheServlet>>(){});
		JettyModule.extend(binder).addStaticServlet("static", "/swagger-ui/static/*");
	}

	@Provides
	@Singleton
	private MappedServlet<SwaggerUiMustacheServlet> provideJerseyServlet(ConfigurationFactory configFactory) {
		return configFactory
				.config(SwaggerUiFactory.class, configPrefix)
				.initUrlPattern("/swagger-ui/")
				.createJerseyServlet();
	}
}
