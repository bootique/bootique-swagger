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
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.jetty.JettyModule;

import java.net.URL;

/**
 * @since  0.26
 */
public class SwaggerUiModule extends ConfigModule {

    public static final String RESOURCE_BASE = "bq.jetty.servlets.swagger-ui.params.resourceBase";

    @Override
    public void configure(Binder binder) {
        URL resource = this.getClass().getClassLoader().getResource("console/");
        BQCoreModule.extend(binder).setProperty(RESOURCE_BASE, resource.toString());
        JettyModule.extend(binder).addStaticServlet("swagger-ui", "/swagger-ui/*");
    }
}
