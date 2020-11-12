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

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Iterator;

/**
 * A convenience superclass for custom model converters that simplifies type detection.
 *
 * @since 2.0.B1
 */
public abstract class BaseModelConverter implements ModelConverter {

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {

        Schema existing = context.resolve(type);
        if (existing != null) {
            return existing;
        }

        TypeWrapper wrapped = TypeWrapper.forType(type.getType());
        return willResolve(type, context, wrapped)
                ? doResolve(type, context, chain, wrapped)
                : delegateResolve(type, context, chain);
    }

    protected abstract boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped);

    protected abstract Schema doResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain, TypeWrapper wrapped);

    protected Schema delegateResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
    }

    protected Schema onSchemaResolved(AnnotatedType type, ModelConverterContext context, Schema resolved) {
        context.defineModel(resolved.getName(), resolved);
        return type.isResolveAsRef()
                ? new Schema().$ref(RefUtils.constructRef(resolved.getName()))
                : resolved;
    }
}
