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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jetty.MappedServlet;
import io.bootique.swagger.ui.mustache.SwaggerUiMustacheServlet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * @since  0.26
 */
@BQConfig
public class SwaggerUiFactory {

	private String specUrl;
	private String urlPattern;

	public String getSpecUrl() {
		return specUrl;
	}

	@BQConfigProperty
	public void setSpecUrl(String specUrl) {
			this.specUrl = specUrl;
	}

	public SwaggerUiFactory initUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
		return this;
	}

	public MappedServlet<SwaggerUiMustacheServlet> createJerseyServlet() {
		SwaggerUiMustacheServlet servlet = new SwaggerUiMustacheServlet(specUrl);
		Set<String> urlPatterns = Collections.singleton(Objects.requireNonNull(urlPattern));
		return new MappedServlet<>(servlet, urlPatterns, "swagger");
	}

}
