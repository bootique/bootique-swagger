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
package io.bootique.swagger.converter;

import io.bootique.swagger.BaseModelConverter;
import io.bootique.swagger.TypeWrapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.time.ZoneOffset;
import java.util.Iterator;

/**
 * @since 3.0
 */
public class ZoneOffsetConverter extends BaseModelConverter {

    static final String PATTERN = "^[-+]\\d\\d:\\d\\d$";

    @Override
    protected boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {
        return wrapped.getRawClass().equals(ZoneOffset.class);
    }

    @Override
    protected Schema doResolve(
            AnnotatedType type,
            ModelConverterContext context,
            Iterator<ModelConverter> chain,
            TypeWrapper wrapped) {
        return new StringSchema().pattern(PATTERN);
    }
}
