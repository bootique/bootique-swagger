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

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.MappedServlet;

import jakarta.inject.Singleton;


public class SwaggerUiModule implements BQModule {

    private static final String CONFIG_PREFIX = "swaggerui";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Integrates Swagger web UI console for presenting OpenAPI documentation")
                .config(CONFIG_PREFIX, SwaggerUiServletFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
        JettyModule.extend(binder).addMappedServlet(new TypeLiteral<MappedServlet<SwaggerUiServlet>>() {
        });
    }

    @Provides
    @Singleton
    private MappedServlet<SwaggerUiServlet> provideSwaggerUiServlet(ConfigurationFactory configFactory) {
        return configFactory.config(SwaggerUiServletFactory.class, CONFIG_PREFIX).create();
    }
}
