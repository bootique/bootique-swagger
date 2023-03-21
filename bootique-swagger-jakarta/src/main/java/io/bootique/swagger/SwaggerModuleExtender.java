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

import io.bootique.ModuleExtender;
import io.bootique.di.Binder;
import io.bootique.di.SetBuilder;
import io.swagger.v3.core.converter.ModelConverter;

/**
 * @since 2.0.B1
 */
public class SwaggerModuleExtender extends ModuleExtender<SwaggerModuleExtender> {

    private SetBuilder<ModelConverter> converters;
    private SetBuilder<OpenApiCustomizer> customizers;

    public SwaggerModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public SwaggerModuleExtender initAllExtensions() {
        contributeConverters();
        contributeCustomizers();
        return this;
    }

    public SwaggerModuleExtender addModelConverter(Class<? extends ModelConverter> converterType) {
        contributeConverters().add(converterType);
        return this;
    }

    public SwaggerModuleExtender addModelConverter(ModelConverter converter) {
        contributeConverters().addInstance(converter);
        return this;
    }

    /**
     * @since 3.0
     */
    public SwaggerModuleExtender addApiCustomizer(Class<? extends OpenApiCustomizer> customizerType) {
        contributeCustomizers().add(customizerType);
        return this;
    }

    /**
     * @since 3.0
     */
    public SwaggerModuleExtender addApiCustomizer(OpenApiCustomizer customizer) {
        contributeCustomizers().addInstance(customizer);
        return this;
    }

    protected SetBuilder<ModelConverter> contributeConverters() {
        return converters != null ? converters : (converters = newSet(ModelConverter.class));
    }

    protected SetBuilder<OpenApiCustomizer> contributeCustomizers() {
        return customizers != null ? customizers : (customizers = newSet(OpenApiCustomizer.class));
    }
}
