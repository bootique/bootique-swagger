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

import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.MappedServlet;
import io.bootique.type.TypeRef;

import javax.inject.Singleton;
import java.net.URL;
import java.util.Map;

/**
 * @since 1.0.RC1
 */
public class SwaggerUiModule extends ConfigModule {

    private static final String RESOURCE_BASE = "bq.jetty.servlets.swagger-ui.params.resourceBase";
    private static final String PATH_INFO_ONLY = "bq.jetty.servlets.swagger-ui.params.pathInfoOnly";

    @Override
    public void configure(Binder binder) {

        URL resource = getClass().getClassLoader().getResource("io/bootique/swagger/ui/docroot/");
        JettyModule.extend(binder)
                .setServletParam("swagger-ui", "resourceBase", resource.toString())
                // "pathInfoOnly = true" ensures that the part of the URL matching the servlet path
                // ("/swagger-ui" in our case) is not included in the file path when resolving a static resource.
                .setServletParam("swagger-ui", "pathInfoOnly", "true");

        JettyModule.extend(binder).addMappedServlet(new TypeLiteral<MappedServlet<SwaggerUiServlet>>() {
        });
    }

    @Provides
    @Singleton
    private MappedServlet<SwaggerUiServlet> provideJerseyServlet(ConfigurationFactory configFactory) {

        Map<String, SwaggerUIModelFactory> configs = config(new TypeRef<Map<String, SwaggerUIModelFactory>>() {
        }, configFactory);

        return new SwaggerUiServletFactory(configs).createServlet();
    }
}
