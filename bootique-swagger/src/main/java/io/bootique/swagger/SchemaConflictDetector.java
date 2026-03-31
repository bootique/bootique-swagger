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

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A temporary {@link ModelConverter} installed around a swagger {@link io.swagger.v3.jaxrs2.Reader#read} call
 * to detect Java classes that map to the same OpenAPI schema name.
 *
 * @since 4.0
 */
class SchemaConflictDetector implements ModelConverter {

    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    private final Map<String, Set<String>> schemaNameToClasses = new LinkedHashMap<>();

    @Override
    public Schema resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> chain) {
        Schema result = chain.hasNext() ? chain.next().resolve(annotatedType, context, chain) : null;

        if (result != null && result.get$ref() != null) {
            String ref = result.get$ref();
            if (ref.startsWith(SCHEMA_REF_PREFIX)) {
                String schemaName = ref.substring(SCHEMA_REF_PREFIX.length());
                String className = extractClassName(annotatedType.getType());
                if (className != null) {
                    schemaNameToClasses
                            .computeIfAbsent(schemaName, k -> new LinkedHashSet<>())
                            .add(className);
                }
            }
        }

        return result;
    }

    void warnOnConflicts(Logger logger) {
        schemaNameToClasses.forEach((name, classes) -> {
            if (classes.size() > 1) {
                logger.warn("""
                        ** Multiple classes are mapped to OpenAPI schema '{}'. Only one definition will be included, \
                        resulting in an incorrect model. Conflicting classes: {}""", name, classes);
            }
        });
    }

    private String extractClassName(Type type) {
        if (type instanceof JavaType jt) {
            return jt.getRawClass().getName();
        }
        if (type instanceof Class<?> c) {
            return c.getName();
        }
        if (type instanceof ParameterizedType pt && pt.getRawType() instanceof Class<?> c) {
            return c.getName();
        }
        return null;
    }
}
