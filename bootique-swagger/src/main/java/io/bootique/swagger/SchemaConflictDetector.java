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
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A temporary {@link ModelConverter} installed around a swagger {@link io.swagger.v3.jaxrs2.Reader#read} call
 * to detect Java classes that map to the same OpenAPI schema name, renaming conflicts to ensure all schemas
 * are included in the model.
 */
class SchemaConflictDetector implements ModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaConflictDetector.class);
    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    // tracks which Java class first claimed each schema name
    private final Map<String, String> schemaNameToClass = new HashMap<>();

    @Override
    public Schema resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (!chain.hasNext()) {
            return null;
        }

        String thisClass = extractClassName(annotatedType.getType());

        if (thisClass != null) {
            String proposedName = computeSchemaName(annotatedType);
            if (proposedName != null) {
                String existingClass = schemaNameToClass.get(proposedName);
                if (existingClass != null && !existingClass.equals(thisClass)) {
                    String newName = findUniqueName(proposedName);
                    LOGGER.warn("** Schema name '{}' is already used by '{}'. Renaming '{}' to '{}'.",
                            proposedName, existingClass, thisClass, newName);
                    annotatedType.name(newName);
                }
            }
        }

        Schema result = chain.next().resolve(annotatedType, context, chain);

        if (result != null && result.get$ref() != null && thisClass != null) {
            String ref = result.get$ref();
            if (ref.startsWith(SCHEMA_REF_PREFIX)) {
                String schemaName = ref.substring(SCHEMA_REF_PREFIX.length());
                schemaNameToClass.putIfAbsent(schemaName, thisClass);
            }
        }

        return result;
    }

    private String findUniqueName(String base) {
        int i = 1;
        while (schemaNameToClass.containsKey(base + i)) {
            i++;
        }
        return base + i;
    }

    private String computeSchemaName(AnnotatedType annotatedType) {
        if (annotatedType.getName() != null && !annotatedType.getName().isEmpty()) {
            return annotatedType.getName();
        }
        Class<?> clazz = extractClass(annotatedType.getType());
        if (clazz == null) {
            return null;
        }
        io.swagger.v3.oas.annotations.media.Schema annotation = clazz.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        return clazz.getSimpleName();
    }

    private String extractClassName(Type type) {
        Class<?> clazz = extractClass(type);
        return clazz != null ? clazz.getName() : null;
    }

    private Class<?> extractClass(Type type) {
        if (type instanceof JavaType jt) {
            return jt.getRawClass();
        }
        if (type instanceof Class<?> c) {
            return c;
        }
        if (type instanceof ParameterizedType pt && pt.getRawType() instanceof Class<?> c) {
            return c;
        }
        return null;
    }
}
