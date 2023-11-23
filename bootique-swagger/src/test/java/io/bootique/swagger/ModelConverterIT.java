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

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.resource.ResourceFactory;
import io.bootique.swagger.config5.X;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.util.Iterator;

@BQTest
public class ModelConverterIT {

    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("-s", "-c", "classpath:config5/startup.yml")
            .autoLoadModules()
            .module(jetty.moduleReplacingConnectors())
            .module(b -> SwaggerModule.extend(b).addModelConverter(XConverter.class))
            .createRuntime();

    @Test
    public void customConverter() {
        Response r = jetty.getTarget().path("/model.json").request().get();
        JettyTester.assertOk(r)
                .assertContent(new ResourceFactory("classpath:config5/response.json"));
    }

    public static final class XConverter extends BaseModelConverter {

        @Override
        protected boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {
            return wrapped.getRawClass().equals(X.class);
        }

        @Override
        protected Schema doResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain, TypeWrapper wrapped) {
            return new StringSchema().description("I am an X");
        }
    }
}
